import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random.nextInt
import scala.math
import regfile.RegFile
object MyUtil {
  def shuffleArray(array:Array[Int]): Array[Int] = {
    for(idx <- array.indices){
      val randomIdx = nextInt(array.length)
      val tempSwap = array(randomIdx)
      array(randomIdx) = array(idx)
      array(idx) = tempSwap
    }
    array
  }
}
object RegFileChiselTest {
  def allWrite(dut:RegFile[UInt], scoreboard:Array[Int]):Unit = {
    val set = scoreboard.length
    val wlength = dut.io.w.length
    val dwidth = dut.io.w.head.data.getWidth
    val wclocks = if(set % wlength == 0) set / wlength else set / wlength + 1
    val idxShuffle = MyUtil.shuffleArray((0 until wclocks * wlength).toArray)
    val portIdxMatrix = Seq.tabulate(wclocks, wlength)((clock:Int, port:Int) => idxShuffle(clock * wlength + port))
    for(portsPerClock <- portIdxMatrix){
      for((w,p) <- dut.io.w.zip(portsPerClock)){
        if(p < set) {
          val data = nextInt(math.pow(2, dwidth).toInt)
          w.addr.poke(p.U)
          w.en.poke(true.B)
          w.data.poke(data.U)
          scoreboard(p) = data
          dut.clock.step()
          w.en.poke(false.B)
        }
      }
    }
  }

  def allReadWithCheck(dut:RegFile[UInt], scoreboard:Array[Int]):Unit = {
    val r = dut.io.r
    for (idx <- scoreboard.indices) {
      for (rport <- r) {
        rport.addr.poke(idx.U)
        rport.en.poke(true.B)
      }
      dut.clock.step()
      for (rport <- r) {
        rport.data.expect(scoreboard(idx).U)
        rport.en.poke(false.B)
      }
      r.foreach(_.en.poke(false.B))
    }
  }

  def checkBypass(dut:RegFile[UInt], scoreboard:Array[Int]):Unit = {
    val dwidth = dut.io.w.head.data.getWidth
    val rportIdx = Array[Int](dut.io.r.length)
    dut.io.r.indices.copyToArray(rportIdx)
    val wportIdx = Array[Int](dut.io.w.length)
    dut.io.w.indices.copyToArray(wportIdx)
    val bypassReadPorts = MyUtil.shuffleArray(rportIdx).toSeq.slice(0,2)
    val bypassWritePorts = MyUtil.shuffleArray(wportIdx).toSeq.head
    val bypassData = nextInt(math.pow(2, dwidth).toInt)
    val bypassAddr = nextInt(scoreboard.length)
    for((w, idx) <- dut.io.w.zipWithIndex){
      val data = nextInt(math.pow(2, dwidth).toInt)
      w.data.poke(if(idx == bypassWritePorts) bypassData.U else data.U)
      w.addr.poke(if(idx == bypassWritePorts) bypassAddr.U else 0.U)
      w.en.poke(if(idx == bypassWritePorts) true.B else false.B)
      if(idx == bypassWritePorts){
        scoreboard(idx) = bypassData
      }
    }
    for((r, idx) <- dut.io.r.zipWithIndex){
      r.addr.poke(if(bypassReadPorts.contains(idx)) bypassAddr.U else 0.U)
      r.en.poke(if(bypassReadPorts.contains(idx)) true.B else false.B)
    }
    dut.clock.step()
    for((r, idx) <- dut.io.r.zipWithIndex){
      if(bypassReadPorts.contains(idx)){
        r.data.expect(bypassData.U)
      }
    }
  }
}
class RegFileChiselTest extends AnyFlatSpec with ChiselScalatestTester{
  behavior of "RegFile"
  val dataWidth = nextInt(64) + 1
  val readPort = nextInt(10) + 2
  val writePort = nextInt(4) + 1
  val set = nextInt(128) + 1

  it should "test all sets writing and reading" in {
    test(new RegFile(UInt(dataWidth.W), set, readPort, writePort)) { m =>
      val scoreboard = new Array[Int](set)
      RegFileChiselTest.allWrite(m, scoreboard)
      RegFileChiselTest.allReadWithCheck(m, scoreboard)
    }
  }
  it should "test write bypass behaviors" in {
    test(new RegFile(UInt(dataWidth.W), set, readPort, writePort)) { m =>
      val scoreboard = new Array[Int](set)
      RegFileChiselTest.allWrite(m, scoreboard)
      for(_ <- 0 until 32) RegFileChiselTest.checkBypass(m, scoreboard)
    }
  }
}

