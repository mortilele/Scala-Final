package utils

import java.io._
import scala.io.Source

object FileUtil {

  def readFile(fileName: String): Seq[String] = {
    val bufferedSource = Source.fromFile(fileName, "utf-8")
    val lines = (for (line <- bufferedSource.getLines()) yield line).toList
    bufferedSource.close
    lines
  }

  def writeFile(fileName: String, data: String): Unit = {
    val file = new File(fileName)
    val bw = new BufferedWriter(new FileWriter(file))
    try {
      bw.write(data)

    } finally {
      bw.close()
    }
  }
}
