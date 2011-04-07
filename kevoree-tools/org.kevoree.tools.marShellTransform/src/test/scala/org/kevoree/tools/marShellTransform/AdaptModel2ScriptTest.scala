package org.kevoree.tools.marShellTransform

import org.scalatest.junit.JUnitSuite
import org.junit._
import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
import org.kevoree.kompare.KevoreeKompareBean

class AdaptModel2ScriptTest extends KevSTestSuiteHelper {

  @Test def adapt2Script() {

    val baseModel = model("baseModel/defaultLibrary.kev")
    val oscript = getScript("scripts/scriptModel1.kevs");

    assume(oscript.interpret(KevsInterpreterContext(baseModel)))
    baseModel.testSave("results", "scriptModel1Result.kev")

    val kompareBean = new KevoreeKompareBean

    val adaptationModel = kompareBean.kompare(emptyModel, baseModel, "duke")

    val script = AdaptationModelWrapper.generateScriptFromAdaptModel(adaptationModel)

    script.blocks.foreach(block => {
      block.l.foreach {
        s =>
          println(s)
      }
    })

  }

}