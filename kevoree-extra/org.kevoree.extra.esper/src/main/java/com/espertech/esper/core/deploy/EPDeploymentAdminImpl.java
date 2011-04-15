/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core.deploy;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPServiceProviderIsolated;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.deploy.*;
import com.espertech.esper.core.EPAdministratorSPI;
import com.espertech.esper.core.StatementEventTypeRef;
import com.espertech.esper.core.StatementIsolationService;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.EventTypeSPI;
import com.espertech.esper.util.DependencyGraph;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

public class EPDeploymentAdminImpl implements EPDeploymentAdmin
{
    public static final String newline = System.getProperty("line.separator");
    private static Log log = LogFactory.getLog(EPDeploymentAdminImpl.class);

    private final EPAdministratorSPI epService;
    private final DeploymentStateService deploymentStateService;
    private final StatementEventTypeRef statementEventTypeRef;
    private final EventAdapterService eventAdapterService;
    private final StatementIsolationService statementIsolationService;

    public EPDeploymentAdminImpl(EPAdministratorSPI epService, DeploymentStateService deploymentStateService, StatementEventTypeRef statementEventTypeRef, EventAdapterService eventAdapterService, StatementIsolationService statementIsolationService)
    {
        this.epService = epService;
        this.deploymentStateService = deploymentStateService;
        this.statementEventTypeRef = statementEventTypeRef;
        this.eventAdapterService = eventAdapterService;
        this.statementIsolationService = statementIsolationService;
    }

    public Module read(InputStream stream, String uri) throws IOException, ParseException
    {
        if (log.isDebugEnabled()) {
            log.debug("Reading module from input stream");
        }
        return readInternal(stream, uri);
    }

    public Module read(File file) throws IOException, ParseException {
        if (log.isDebugEnabled()) {
            log.debug("Reading resource '" + file.getAbsolutePath() + "'");
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return readInternal(inputStream, file.getAbsolutePath());
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.debug("Error closing input stream", e);
                }
            }            
        }
    }

    public Module read(URL url) throws IOException, ParseException {
        if (log.isDebugEnabled())
        {
            log.debug( "Reading resource from url: " + url.toString() );
        }
        return readInternal(url.openStream(), url.toString());
    }

    public Module read(String resource) throws IOException, ParseException {
        if (log.isDebugEnabled()) {
            log.debug("Reading resource '" + resource + "'");
        }
        String stripped = resource.startsWith("/") ? resource.substring(1) : resource;

        InputStream stream = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader!=null) {
            stream = classLoader.getResourceAsStream( stripped );
        }
        if ( stream == null ) {
            stream = EPDeploymentAdminImpl.class.getResourceAsStream( resource );
        }
        if ( stream == null ) {
            stream = EPDeploymentAdminImpl.class.getClassLoader().getResourceAsStream( stripped );
        }
        if ( stream == null ) {
           throw new IOException("Failed to find resource '" + resource + "' in classpath");
        }

        try {
            return readInternal(stream, resource);
        }
        finally {
            try {
                stream.close();
            } catch (IOException e) {
                log.debug("Error closing input stream", e);
            }
        }
    }

    public synchronized DeploymentResult deploy(Module module, DeploymentOptions options) throws DeploymentActionException
    {
        String deploymentId = deploymentStateService.nextDeploymentId();
        return deployInternal(module, options, deploymentId, Calendar.getInstance());
    }

    private DeploymentResult deployInternal(Module module, DeploymentOptions options, String deploymentId, Calendar addedDate) throws DeploymentActionException
    {
        if (options == null) {
            options = new DeploymentOptions();
        }

        if (log.isDebugEnabled()) {
            log.debug("Deploying module " + module);
        }
        if (module.getImports() != null) {
            for (String imported : module.getImports()) {
                if (log.isDebugEnabled()) {
                    log.debug("Adding import " + imported);
                }
                epService.getConfiguration().addImport(imported);
            }
        }

        if (options.isCompile()) {
            List<DeploymentItemException> exceptions = new ArrayList<DeploymentItemException>();
            for (ModuleItem item : module.getItems()) {
                try {
                    epService.compileEPL(item.getExpression());
                }
                catch (EPException ex) {
                    exceptions.add(new DeploymentItemException(ex.getMessage(), item.getExpression(), ex, item.getLineNumber()));
                }
            }

            if (!exceptions.isEmpty()) {
                throw buildException("Compilation failed", module, exceptions);
            }
        }

        if (options.isCompileOnly()) {
            return null;
        }

        List<DeploymentItemException> exceptions = new ArrayList<DeploymentItemException>();
        List<DeploymentInformationItem> statementNames = new ArrayList<DeploymentInformationItem>();
        List<EPStatement> statements = new ArrayList<EPStatement>();
        Set<String> eventTypesReferenced = new HashSet<String>();

        for (ModuleItem item : module.getItems()) {
            try {

                EPStatement stmt;
                if (options.getIsolatedServiceProvider() == null) {
                    stmt = epService.createEPL(item.getExpression());
                }
                else {
                    EPServiceProviderIsolated unit = statementIsolationService.getIsolationUnit(options.getIsolatedServiceProvider(), -1);
                    stmt = unit.getEPAdministrator().createEPL(item.getExpression(), null, null);
                }
                statementNames.add(new DeploymentInformationItem(stmt.getName(), stmt.getText()));
                statements.add(stmt);

                Set<String> types = statementEventTypeRef.getTypesForStatementName(stmt.getName());
                if (types != null) {
                    eventTypesReferenced.addAll(types);
                }
            }
            catch (EPException ex) {
                exceptions.add(new DeploymentItemException(ex.getMessage(), item.getExpression(), ex, item.getLineNumber()));
                if (options.isFailFast()) {
                    break;                        
                }
            }
        }

        if (!exceptions.isEmpty()) {
            if (options.isRollbackOnFail()) {
                log.debug("Rolling back intermediate statements for deployment");
                for (EPStatement stmt : statements) {
                    try {
                        stmt.destroy();
                    }
                    catch (Exception ex) {
                        log.debug("Failed to destroy created statement during rollback: " + ex.getMessage(), ex);
                    }
                }
                undeployTypes(eventTypesReferenced);
            }
            String text = "Deployment failed";
            if (options.isValidateOnly()) {
                text = "Validation failed";
            }
            throw buildException(text, module, exceptions);
        }

        if (options.isValidateOnly()) {
            log.debug("Rolling back created statements for validate-only");
            for (EPStatement stmt : statements) {
                try {
                    stmt.destroy();
                }
                catch (Exception ex) {
                    log.debug("Failed to destroy created statement during rollback: " + ex.getMessage(), ex);
                }
            }
            undeployTypes(eventTypesReferenced);
            return null;
        }

        DeploymentInformationItem[] deploymentInfoArr = statementNames.toArray(new DeploymentInformationItem[statementNames.size()]);
        DeploymentInformation desc = new DeploymentInformation(deploymentId, module, addedDate, Calendar.getInstance(), deploymentInfoArr, DeploymentState.DEPLOYED);
        deploymentStateService.addUpdateDeployment(desc);

        if (log.isDebugEnabled()) {
            log.debug("Module " + module + " was successfully deployed.");
        }
        return new DeploymentResult(desc.getDeploymentId(), Collections.unmodifiableList(statements));
    }

    private DeploymentActionException buildException(String msg, Module module, List<DeploymentItemException> exceptions)
    {
        String message = msg;
        if (module.getName() != null) {
            message += " in module '" + module.getName() + "'";
        }
        if (module.getUri() != null) {
            message += " in module url '" + module.getUri() + "'";
        }
        if (exceptions.size() > 0) {
            message += " : " + exceptions.get(0).getMessage();
        }
        return new DeploymentActionException(message, exceptions);
    }

    public Module parse(String eplModuleText) throws IOException, ParseException
    {
        return parseInternal(eplModuleText, null);
    }

    public synchronized UndeploymentResult undeployRemove(String deploymentId) throws DeploymentNotFoundException {
        DeploymentInformation info = deploymentStateService.getDeployment(deploymentId);
        if (info == null) {
            throw new DeploymentNotFoundException("Deployment by id '" + deploymentId + "' could not be found");
        }

        UndeploymentResult result;
        if (info.getState() == DeploymentState.DEPLOYED) {
            result = undeployRemoveInternal(info);
        }
        else {
            result = new UndeploymentResult(deploymentId, Collections.<DeploymentInformationItem>emptyList());
        }
        deploymentStateService.remove(deploymentId);
        return result;
    }

    public synchronized UndeploymentResult undeploy(String deploymentId) throws DeploymentStateException, DeploymentNotFoundException
    {
        DeploymentInformation info = deploymentStateService.getDeployment(deploymentId);
        if (info == null) {
            throw new DeploymentNotFoundException("Deployment by id '" + deploymentId + "' could not be found");
        }
        if (info.getState() == DeploymentState.UNDEPLOYED) {
            throw new DeploymentStateException("Deployment by id '" + deploymentId + "' is already in undeployed state");
        }
        UndeploymentResult result = undeployRemoveInternal(info);
        DeploymentInformation updated = new DeploymentInformation(deploymentId, info.getModule(), info.getAddedDate(), Calendar.getInstance(), new DeploymentInformationItem[0], DeploymentState.UNDEPLOYED);
        deploymentStateService.addUpdateDeployment(updated);
        return result;
    }

    private UndeploymentResult undeployRemoveInternal(DeploymentInformation info)
    {
        DeploymentInformationItem[] reverted = new DeploymentInformationItem[info.getItems().length];
        for (int i = 0; i < info.getItems().length; i++) {
            reverted[i] = info.getItems()[info.getItems().length - 1 - i];
        }

        List<DeploymentInformationItem> revertedStatements = new ArrayList<DeploymentInformationItem>();
        Set<String> referencedTypes = new HashSet<String>();
        for (DeploymentInformationItem item : reverted) {
            EPStatement statement = epService.getStatement(item.getStatementName());
            if (statement == null) {
                log.debug("Deployment id '" + info.getDeploymentId() + "' statement name '" + item + "' not found");
                continue;
            }
            referencedTypes.addAll(statementEventTypeRef.getTypesForStatementName(statement.getName()));
            if (statement.isDestroyed()) {
                continue;
            }
            try {
                statement.destroy();
            }
            catch (RuntimeException ex) {
                log.warn("Unexpected exception destroying statement: " + ex.getMessage(), ex);
            }
            revertedStatements.add(item);
        }
        undeployTypes(referencedTypes);

        Collections.reverse(revertedStatements);
        return new UndeploymentResult(info.getDeploymentId(), revertedStatements);
    }

    public synchronized String[] getDeployments()
    {
        return deploymentStateService.getDeployments();
    }

    public synchronized DeploymentInformation getDeployment(String deploymentId)
    {
        return deploymentStateService.getDeployment(deploymentId);
    }

    public synchronized DeploymentInformation[] getDeploymentInformation()
    {
        return deploymentStateService.getAllDeployments();
    }

    public synchronized DeploymentOrder getDeploymentOrder(Collection<Module> modules, DeploymentOrderOptions options) throws DeploymentOrderException
    {
        if (options == null) {
            options = new DeploymentOrderOptions();
        }
        String[] deployments = deploymentStateService.getDeployments();

        List<Module> proposedModules = new ArrayList<Module>();
        proposedModules.addAll(modules);

        Set<String> availableModuleNames = new HashSet<String>();
        for (Module proposedModule : proposedModules) {
            if (proposedModule.getName() != null) {
                availableModuleNames.add(proposedModule.getName());
            }
        }

        // Collect all uses-dependencies of existing modules
        Map<String, Set<String>> usesPerModuleName = new HashMap<String, Set<String>>();
        for (String deployment : deployments) {
            DeploymentInformation info = deploymentStateService.getDeployment(deployment);
            if (info == null) {
                continue;
            }
            if ((info.getModule().getName() == null) || (info.getModule().getUses() == null)) {
                continue;
            }
            Set<String> usesSet = usesPerModuleName.get(info.getModule().getName());
            if (usesSet == null) {
                usesSet = new HashSet<String>();
                usesPerModuleName.put(info.getModule().getName(), usesSet);
            }
            usesSet.addAll(info.getModule().getUses());
        }

        // Collect uses-dependencies of proposed modules
        for (Module proposedModule : proposedModules) {

            // check uses-dependency is available
            if (options.isCheckUses()) {
                if (proposedModule.getUses() != null) {
                    for (String uses : proposedModule.getUses()) {
                        if (availableModuleNames.contains(uses)) {
                            continue;
                        }
                        if (isDeployed(uses)) {
                            continue;
                        }
                        String message = "Module-dependency not found";
                        if (proposedModule.getName() != null) {
                            message += " as declared by module '" + proposedModule.getName() + "'";
                        }
                        message += " for uses-declaration '" + uses + "'";
                        throw new DeploymentOrderException(message);
                    }
                }
            }
            
            if ((proposedModule.getName() == null) || (proposedModule.getUses() == null)) {
                continue;
            }
            Set<String> usesSet = usesPerModuleName.get(proposedModule.getName());
            if (usesSet == null) {
                usesSet = new HashSet<String>();
                usesPerModuleName.put(proposedModule.getName(), usesSet);
            }
            usesSet.addAll(proposedModule.getUses());
        }

        Map<String, SortedSet<Integer>> proposedModuleNames = new HashMap<String, SortedSet<Integer>>();
        int count = 0;
        for (Module proposedModule : proposedModules) {
            SortedSet<Integer> moduleNumbers = proposedModuleNames.get(proposedModule.getName());
            if (moduleNumbers == null) {
                moduleNumbers = new TreeSet<Integer>();
                proposedModuleNames.put(proposedModule.getName(), moduleNumbers);
            }
            moduleNumbers.add(count);
            count++;
        }

        DependencyGraph graph = new DependencyGraph(proposedModules.size());
        int fromModule = 0;
        for (Module proposedModule : proposedModules) {
            if ((proposedModule.getUses() == null) || (proposedModule.getUses().isEmpty())) {
                fromModule++;
                continue;
            }
            SortedSet<Integer> dependentModuleNumbers = new TreeSet<Integer>();
            for (String use : proposedModule.getUses()) {
                SortedSet<Integer> moduleNumbers = proposedModuleNames.get(use);
                if (moduleNumbers == null) {
                    continue;
                }
                dependentModuleNumbers.addAll(moduleNumbers);
            }
            dependentModuleNumbers.remove(fromModule);
            graph.addDependency(fromModule, dependentModuleNumbers);
            fromModule++;
        }

        if (options.isCheckCircularDependency()) {
            Stack<Integer> circular = graph.getFirstCircularDependency();
            if (circular != null) {
                String message = "";
                String delimiter = "";
                for (int i : circular) {
                    message += delimiter;
                    message += "module '" + proposedModules.get(i).getName() + "'";
                    delimiter = " uses (depends on) ";
                }
                throw new DeploymentOrderException("Circular dependency detected in module uses-relationships: " + message);
            }
        }

        List<Module> reverseDeployList = new ArrayList<Module>();
        Set<Integer> ignoreList = new HashSet<Integer>();
        while(ignoreList.size() < proposedModules.size()) {

            // seconardy sort according to the order of listing
            Set<Integer> rootNodes = new TreeSet<Integer>(new Comparator<Integer>() {
                public int compare(Integer o1, Integer o2)
                {
                    return -1 * o1.compareTo(o2);
                }
            });
            rootNodes.addAll(graph.getRootNodes(ignoreList));

            if (rootNodes.isEmpty()) {   // circular dependency could cause this
                for (int i = 0; i < proposedModules.size(); i++) {
                    if (!ignoreList.contains(i)) {
                        rootNodes.add(i);
                        break;
                    }
                }
            }

            for (Integer root : rootNodes) {
                ignoreList.add(root);
                reverseDeployList.add(proposedModules.get(root));
            }
        }
        
        Collections.reverse(reverseDeployList);
        return new DeploymentOrder(reverseDeployList);
    }

    public synchronized boolean isDeployed(String moduleName) {
        DeploymentInformation[] infos = deploymentStateService.getAllDeployments();
        if (infos == null) {
            return false;
        }
        for (DeploymentInformation info : infos) {
            if ((info.getModule().getName() != null) && (info.getModule().getName().equals(moduleName))) {
                return info.getState() == DeploymentState.DEPLOYED;
            }
        }
        return false;
    }

    public synchronized DeploymentResult readDeploy(InputStream stream, String moduleURI, String moduleArchive, Object userObject) throws IOException, ParseException, DeploymentOrderException, DeploymentActionException
    {
        Module module = readInternal(stream, moduleURI);
        return deployQuick(module, moduleURI, moduleArchive, userObject);
    }

    public synchronized DeploymentResult readDeploy(String resource, String moduleURI, String moduleArchive, Object userObject) throws IOException, ParseException, DeploymentOrderException, DeploymentActionException
    {
        Module module = read(resource);
        return deployQuick(module, moduleURI, moduleArchive, userObject);
    }

    public synchronized DeploymentResult parseDeploy(String buffer, String moduleURI, String moduleArchive, Object userObject) throws IOException, ParseException, DeploymentOrderException, DeploymentActionException
    {
        Module module = parseInternal(buffer, moduleURI);
        return deployQuick(module, moduleURI, moduleArchive, userObject);
    }

    public synchronized String add(Module module)
    {
        String deploymentId = deploymentStateService.nextDeploymentId();
        DeploymentInformation desc = new DeploymentInformation(deploymentId, module, Calendar.getInstance(), Calendar.getInstance(), new DeploymentInformationItem[0], DeploymentState.UNDEPLOYED);
        deploymentStateService.addUpdateDeployment(desc);
        return deploymentId;
    }

    public synchronized DeploymentResult deploy(String deploymentId, DeploymentOptions options) throws DeploymentNotFoundException, DeploymentStateException, DeploymentOrderException, DeploymentActionException
    {
        DeploymentInformation info = deploymentStateService.getDeployment(deploymentId);
        if (info == null) {
            throw new DeploymentNotFoundException("Deployment by id '" + deploymentId + "' could not be found");
        }
        if (info.getState() == DeploymentState.DEPLOYED) {
            throw new DeploymentStateException("Module by deployment id '" + deploymentId + "' is already in deployed state");
        }
        getDeploymentOrder(Collections.singletonList(info.getModule()), null);
        return deployInternal(info.getModule(), options, deploymentId, info.getAddedDate());
    }

    public synchronized void remove(String deploymentId) throws DeploymentStateException, DeploymentNotFoundException
    {
        DeploymentInformation info = deploymentStateService.getDeployment(deploymentId);
        if (info == null) {
            throw new DeploymentNotFoundException("Deployment by id '" + deploymentId + "' could not be found");
        }
        if (info.getState() == DeploymentState.DEPLOYED) {
            throw new DeploymentStateException("Deployment by id '" + deploymentId + "' is in deployed state, please undeploy first");
        }
        deploymentStateService.remove(deploymentId);
    }

    private DeploymentResult deployQuick(Module module, String moduleURI, String moduleArchive, Object userObject) throws IOException, ParseException, DeploymentOrderException, DeploymentActionException
    {
        module.setUri(moduleURI);
        module.setArchiveName(moduleArchive);
        module.setUserObject(userObject);
        getDeploymentOrder(Collections.singletonList(module), null);
        return deploy(module, null);
    }

    private Module readInternal(InputStream stream, String resourceName) throws IOException, ParseException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringWriter buffer = new StringWriter();
        String strLine;
        while ((strLine = br.readLine()) != null)   {
            buffer.append(strLine);
            buffer.append(newline);
        }
        stream.close();

        return parseInternal(buffer.toString(), resourceName);
    }

    private Module parseInternal(String buffer, String resourceName) throws IOException, ParseException {

        List<EPLModuleParseItem> semicolonSegments = EPLModuleUtil.parse(buffer.toString());
        List<ParseNode> nodes = new ArrayList<ParseNode>();
        for (EPLModuleParseItem segment : semicolonSegments) {
            nodes.add(EPLModuleUtil.getModule(segment, resourceName));
        }

        String moduleName = null;
        int count = 0;
        for (ParseNode node : nodes) {
            if (node instanceof ParseNodeComment) {
                continue;
            }
            if (node instanceof ParseNodeModule) {
                if (moduleName != null) {
                    throw new ParseException("Duplicate use of the 'module' keyword for resource '" + resourceName + "'");
                }
                if (count > 0) {
                    throw new ParseException("The 'module' keyword must be the first declaration in the module file for resource '" + resourceName + "'");
                }
                moduleName = ((ParseNodeModule) node).getModuleName();
            }
            count++;
        }

        Set<String> uses = new LinkedHashSet<String>();
        Set<String> imports = new LinkedHashSet<String>();
        count = 0;
        for (ParseNode node : nodes) {
            if ((node instanceof ParseNodeComment) || (node instanceof ParseNodeModule)) {
                continue;
            }
            String message = "The 'uses' and 'import' keywords must be the first declaration in the module file or follow the 'module' declaration";
            if (node instanceof ParseNodeUses) {
                if (count > 0) {
                    throw new ParseException(message);
                }
                uses.add(((ParseNodeUses) node).getUses());
                continue;
            }
            if (node instanceof ParseNodeImport) {
                if (count > 0) {
                    throw new ParseException(message);
                }
                imports.add(((ParseNodeImport) node).getImported());
                continue;
            }
            count++;
        }

        List<ModuleItem> items = new ArrayList<ModuleItem>();
        for (ParseNode node : nodes) {
            if ((node instanceof ParseNodeComment) || (node instanceof ParseNodeExpression)) {
                boolean isComments = (node instanceof ParseNodeComment);
                items.add(new ModuleItem(node.getItem().getExpression(), isComments, node.getItem().getLineNum(), node.getItem().getStartChar(), node.getItem().getEndChar()));
            }
        }

        return new Module(moduleName, resourceName, uses, imports, items, buffer);
    }

    private void undeployTypes(Set<String> referencedTypes)
    {
        for (String typeName : referencedTypes) {

            boolean typeInUse = statementEventTypeRef.isInUse(typeName);
            if (typeInUse) {
                if (log.isDebugEnabled()) {
                    log.debug("Event type '" + typeName + "' is in use, not removing type");
                }
                continue;
            }

            if (log.isDebugEnabled()) {
                log.debug("Event type '" + typeName + "' is no longer in use, removing type");
            }
            EventType type = eventAdapterService.getExistsTypeByName(typeName);
            if (type != null) {
                EventTypeSPI spi = (EventTypeSPI) type;
                if (!spi.getMetadata().isApplicationPreConfigured()) {
                    eventAdapterService.removeType(typeName);
                }
            }
        }
    }
}
