package org.scalacoin.script.bitwise

import org.scalacoin.script.{ScriptProgramImpl, ScriptProgram}
import org.scalacoin.script.constant._
import org.scalacoin.script.control.{OP_VERIFY, ControlOperationsInterpreter}
import org.scalacoin.util.ScalacoinUtil
import org.slf4j.LoggerFactory

/**
 * Created by chris on 1/6/16.
 */
trait BitwiseInterpreter extends ControlOperationsInterpreter  {

  private def logger = LoggerFactory.getLogger(this.getClass())

  /**
   * Returns 1 if the inputs are exactly equal, 0 otherwise.
   * @param program
   * @return
   */
  def opEqual(program : ScriptProgram) : ScriptProgram = {
    require(program.stack.size > 1, "Stack size must be 2 or more to compare the top two values for OP_EQUAL")
    require(program.script.headOption.isDefined && program.script.head == OP_EQUAL, "Script operation must be OP_EQUAL")

    logger.debug("Original stack: " + program.stack)
    val h = program.stack.head
    val h1 = program.stack.tail.head

    val result = (h,h1) match {
      case (ScriptConstantImpl(x),ScriptConstantImpl(y)) => x == y
      case (BytesToPushOntoStackImpl(x), BytesToPushOntoStackImpl(y)) => x == y
      case (BytesToPushOntoStackImpl(x), ScriptConstantImpl(y)) => BytesToPushOntoStackImpl(x).hex == y
      case (ScriptConstantImpl(x), BytesToPushOntoStackImpl(y)) => x == BytesToPushOntoStackImpl(y).hex
      case (ScriptConstantImpl(x), ScriptNumberImpl(y)) => ScalacoinUtil.hexToLong(x) == y
      case (ScriptNumberImpl(x), ScriptConstantImpl(y)) => x == ScalacoinUtil.hexToLong(y)
      case (OP_0, x) => OP_0.hex == x.hex
      case (x, OP_0) => x.hex == OP_0.hex
      case (OP_1,x) => OP_1.scriptNumber == x
      case (x,OP_1) => x == OP_1.scriptNumber
      case _ => h == h1
    }

    val scriptBoolean : ScriptBoolean = if (result) ScriptTrue else ScriptFalse

    ScriptProgramImpl(scriptBoolean :: program.stack.tail.tail, program.script.tail,program.transaction, program.altStack)
  }


  /**
   * Same as OP_EQUAL, but runs OP_VERIFY afterward.
   * @param program
   * @return
   */
  def equalVerify(program : ScriptProgram) : ScriptProgram = {
    require(program.stack.size > 1, "Stack size must be 2 or more to compare the top two values")
    require(program.script.headOption.isDefined && program.script.head == OP_EQUALVERIFY, "Script operation must be OP_EQUALVERIFY")
    //first replace OP_EQUALVERIFY with OP_EQUAL and OP_VERIFY
    val simpleScript = OP_EQUAL :: OP_VERIFY :: program.script.tail
    val newProgram: ScriptProgram = opEqual(ScriptProgramImpl(program.stack,
      simpleScript,program.transaction, program.altStack))
    opVerify(newProgram)
  }


}
