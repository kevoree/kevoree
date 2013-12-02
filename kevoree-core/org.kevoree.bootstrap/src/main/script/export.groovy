

outputDir = project.getBuild().getOutputDirectory()

new File("$outputDir/bootinfo").withWriter { out ->
    project.getDependencies().each{
        if(it.getScope() != "test"){
            line = it.getGroupId()+":"+it.getArtifactId()+":"+it.getVersion()
            out.println line
        }
    }
}

