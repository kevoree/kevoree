/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.*;
import com.espertech.esper.client.deploy.EPDeploymentAdmin;
import com.espertech.esper.client.soda.*;
import com.espertech.esper.core.deploy.EPDeploymentAdminImpl;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.spec.*;
import com.espertech.esper.pattern.EvalNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation for the admin interface.
 */
public class EPAdministratorImpl implements EPAdministratorSPI
{
    private EPServicesContext services;
    private ConfigurationOperations configurationOperations;
    private SelectClauseStreamSelectorEnum defaultStreamSelector;
    private EPDeploymentAdmin deploymentAdminService;

    /**
     * Constructor - takes the services context as argument.
     * @param adminContext - administrative context
     */
    public EPAdministratorImpl(EPAdministratorContext adminContext)
    {
        this.services = adminContext.getServices();
        this.configurationOperations = adminContext.getConfigurationOperations();
        this.defaultStreamSelector = adminContext.getDefaultStreamSelector();
        this.deploymentAdminService = new EPDeploymentAdminImpl(this, adminContext.getServices().getDeploymentStateService(), adminContext.getServices().getStatementEventTypeRefService(), adminContext.getServices().getEventAdapterService(), adminContext.getServices().getStatementIsolationService());
    }

    public EPDeploymentAdmin getDeploymentAdmin()
    {
        return deploymentAdminService;
    }

    public EPStatement createPattern(String onExpression) throws EPException
    {
        return createPatternStmt(onExpression, null, null);
    }

    public EPStatement createEPL(String eplStatement) throws EPException
    {
        return createEPLStmt(eplStatement, null, null);
    }

    public EPStatement createPattern(String expression, String statementName) throws EPException
    {
        return createPatternStmt(expression, statementName, null);
    }

    public EPStatement createPattern(String expression, String statementName, Object userObject) throws EPException
    {
        return createPatternStmt(expression, statementName, userObject);
    }

    public EPStatement createEPL(String eplStatement, String statementName) throws EPException
    {
        return createEPLStmt(eplStatement, statementName, null);
    }

    public EPStatement createEPL(String eplStatement, String statementName, Object userObject) throws EPException
    {
        return createEPLStmt(eplStatement, statementName, userObject);
    }

    public EPStatement createPattern(String expression, Object userObject) throws EPException
    {
        return createPatternStmt(expression, null, userObject);
    }

    public EPStatement createEPL(String eplStatement, Object userObject) throws EPException
    {
        return createEPLStmt(eplStatement, null, userObject);
    }

    private EPStatement createPatternStmt(String expression, String statementName, Object userObject) throws EPException
    {
        StatementSpecRaw rawPattern = EPAdministratorHelper.compilePattern(expression, expression, true, services, SelectClauseStreamSelectorEnum.ISTREAM_ONLY);
        return services.getStatementLifecycleSvc().createAndStart(rawPattern, expression, true, statementName, userObject, null);

        /**
         * For round-trip testing of all statements, of a statement to SODA and creation from SODA, use below lines:
        String pattern = "select * from pattern[" + expression + "]";
        EPStatementObjectModel model = compileEPL(pattern);
        return create(model, statementName);
         */
    }

    private EPStatement createEPLStmt(String eplStatement, String statementName, Object userObject) throws EPException
    {
        StatementSpecRaw statementSpec = EPAdministratorHelper.compileEPL(eplStatement, eplStatement, true, statementName, services, defaultStreamSelector);
        EPStatement statement = services.getStatementLifecycleSvc().createAndStart(statementSpec, eplStatement, false, statementName, userObject, null);

        log.debug(".createEPLStmt Statement created and started");
        return statement;

        /**
         * For round-trip testing of all statements, of a statement to SODA and creation from SODA, use below lines:
        EPStatementObjectModel model = compile(eplStatement);
        return create(model, statementName);
         */
    }

    public EPStatement create(EPStatementObjectModel sodaStatement) throws EPException
    {
        return create(sodaStatement, null);
    }

    public EPStatement create(EPStatementObjectModel sodaStatement, String statementName, Object userObject) throws EPException
    {
        // Specifies the statement
        StatementSpecRaw statementSpec = StatementSpecMapper.map(sodaStatement, services.getEngineImportService(), services.getVariableService(), services.getConfigSnapshot(), services.getSchedulingService(), services.getEngineURI(), services.getPatternNodeFactory());
        String eplStatement = sodaStatement.toEPL();

        EPStatement statement = services.getStatementLifecycleSvc().createAndStart(statementSpec, eplStatement, false, statementName, userObject, null);

        log.debug(".createEPLStmt Statement created and started");
        return statement;
    }

    public EPStatement create(EPStatementObjectModel sodaStatement, String statementName) throws EPException
    {
        // Specifies the statement
        StatementSpecRaw statementSpec = StatementSpecMapper.map(sodaStatement, services.getEngineImportService(), services.getVariableService(), services.getConfigSnapshot(), services.getSchedulingService(), services.getEngineURI(), services.getPatternNodeFactory());
        String eplStatement = sodaStatement.toEPL();

        EPStatement statement = services.getStatementLifecycleSvc().createAndStart(statementSpec, eplStatement, false, statementName, null, null);

        log.debug(".createEPLStmt Statement created and started");
        return statement;
    }

    public EPPreparedStatement prepareEPL(String eplExpression) throws EPException
    {
        // compile to specification
        StatementSpecRaw statementSpec = EPAdministratorHelper.compileEPL(eplExpression, eplExpression, true, null, services, defaultStreamSelector);

        // map to object model thus finding all substitution parameters and their indexes
        StatementSpecUnMapResult unmapped = StatementSpecMapper.unmap(statementSpec);

        // the prepared statement is the object model plus a list of substitution parameters
        // map to specification will refuse any substitution parameters that are unfilled
        return new EPPreparedStatementImpl(unmapped.getObjectModel(), unmapped.getIndexedParams());
    }

    public EPPreparedStatement preparePattern(String patternExpression) throws EPException
    {
        StatementSpecRaw rawPattern = EPAdministratorHelper.compilePattern(patternExpression, patternExpression, true, services, SelectClauseStreamSelectorEnum.ISTREAM_ONLY);

        // map to object model thus finding all substitution parameters and their indexes
        StatementSpecUnMapResult unmapped = StatementSpecMapper.unmap(rawPattern);

        // the prepared statement is the object model plus a list of substitution parameters
        // map to specification will refuse any substitution parameters that are unfilled
        return new EPPreparedStatementImpl(unmapped.getObjectModel(), unmapped.getIndexedParams());
    }

    public EPStatement create(EPPreparedStatement prepared, String statementName, Object userObject) throws EPException
    {
        EPPreparedStatementImpl impl = (EPPreparedStatementImpl) prepared;

        StatementSpecRaw statementSpec = StatementSpecMapper.map(impl.getModel(), services.getEngineImportService(), services.getVariableService(), services.getConfigSnapshot(), services.getSchedulingService(), services.getEngineURI(), services.getPatternNodeFactory());
        String eplStatement = impl.getModel().toEPL();

        return services.getStatementLifecycleSvc().createAndStart(statementSpec, eplStatement, false, statementName, userObject, null);
    }

    public EPStatement create(EPPreparedStatement prepared, String statementName) throws EPException
    {
        EPPreparedStatementImpl impl = (EPPreparedStatementImpl) prepared;

        StatementSpecRaw statementSpec = StatementSpecMapper.map(impl.getModel(), services.getEngineImportService(), services.getVariableService(), services.getConfigSnapshot(), services.getSchedulingService(), services.getEngineURI(), services.getPatternNodeFactory());
        String eplStatement = impl.getModel().toEPL();

        return services.getStatementLifecycleSvc().createAndStart(statementSpec, eplStatement, false, statementName, null, null);
    }

    public EPStatement create(EPPreparedStatement prepared) throws EPException
    {
        return create(prepared, null);
    }

    public EPStatementObjectModel compileEPL(String eplStatement) throws EPException
    {
        StatementSpecRaw statementSpec = EPAdministratorHelper.compileEPL(eplStatement, eplStatement, true, null, services, defaultStreamSelector);
        StatementSpecUnMapResult unmapped = StatementSpecMapper.unmap(statementSpec);
        if (unmapped.getIndexedParams().size() != 0)
        {
            throw new EPException("Invalid use of substitution parameters marked by '?' in statement, use the prepare method to prepare statements with substitution parameters");
        }
        return unmapped.getObjectModel();
    }

    public EPStatement getStatement(String name)
    {
        return services.getStatementLifecycleSvc().getStatementByName(name);
    }

    public String[] getStatementNames()
    {
        return services.getStatementLifecycleSvc().getStatementNames();
    }

    public void startAllStatements() throws EPException
    {
        services.getStatementLifecycleSvc().startAllStatements();
    }

    public void stopAllStatements() throws EPException
    {
        services.getStatementLifecycleSvc().stopAllStatements();
    }

    public void destroyAllStatements() throws EPException
    {
        services.getStatementLifecycleSvc().destroyAllStatements();
    }

    public ConfigurationOperations getConfiguration()
    {
        return configurationOperations;
    }

    /**
     * Destroys an engine instance.
     */
    public void destroy()
    {
        services = null;
        configurationOperations = null;
    }

    public EvalNode compilePatternToNode(String pattern) throws EPException
    {
        StatementSpecRaw raw = EPAdministratorHelper.compilePattern(pattern, pattern, false, services, SelectClauseStreamSelectorEnum.ISTREAM_ONLY);
        return ((PatternStreamSpecRaw) raw.getStreamSpecs().get(0)).getEvalNode();    
    }

    public ExprNode compileExpression(String expression) throws EPException
    {
        String toCompile = "select * from java.lang.Object.win:time(" + expression + ")";
        StatementSpecRaw raw = EPAdministratorHelper.compileEPL(toCompile, expression, false, null, services, SelectClauseStreamSelectorEnum.ISTREAM_ONLY);
        return raw.getStreamSpecs().get(0).getViewSpecs().get(0).getObjectParameters().get(0);
    }

    private static String getNullableErrortext(String msg, String cause)
    {
        if (cause == null)
        {
            return msg;
        }
        else
        {
            return msg + ": " + cause;
        }
    }

    public Expression compileExpressionToSODA(String expression) throws EPException
    {
        ExprNode node = compileExpression(expression);
        return StatementSpecMapper.unmap(node);
    }

    public PatternExpr compilePatternToSODA(String expression) throws EPException
    {
        EvalNode node = compilePatternToNode(expression);
        return StatementSpecMapper.unmap(node);
    }

    public AnnotationPart compileAnnotationToSODA(String annotationExpression)
    {
        String toCompile = annotationExpression + " select * from java.lang.Object";
        StatementSpecRaw raw = EPAdministratorHelper.compileEPL(toCompile, annotationExpression, false, null, services, SelectClauseStreamSelectorEnum.ISTREAM_ONLY);
        return StatementSpecMapper.unmap(raw.getAnnotations().get(0));
    }

    public MatchRecognizeRegEx compileMatchRecognizePatternToSODA(String matchRecogPatternExpression)
    {
        String toCompile = "select * from java.lang.Object match_recognize(measures a.b as c pattern (" + matchRecogPatternExpression + ") define A as true)";
        StatementSpecRaw raw = EPAdministratorHelper.compileEPL(toCompile, matchRecogPatternExpression, false, null, services, SelectClauseStreamSelectorEnum.ISTREAM_ONLY);
        return StatementSpecMapper.unmap(raw.getMatchRecognizeSpec().getPattern());
    }

    private static Log log = LogFactory.getLog(EPAdministratorImpl.class);
}
