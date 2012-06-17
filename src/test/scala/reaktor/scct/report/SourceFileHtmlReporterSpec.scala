package reaktor.scct.report

import org.specs.Specification
import xml.Text
import org.specs.matcher.XmlMatchers
import reaktor.scct.{Env, ClassTypes, Name, CoveredBlock}
import java.io.File

class SourceFileHtmlReporterSpec extends Specification with XmlMatchers {

  "Single line formatting" should {
    val sut = new SourceFileHtmlReporter("src", new CoverageData(Nil), List())

    "format covered line" in {
      sut.formatLine("my line", 0, blocks((0, true))) must equalIgnoreSpace(Text("my line"))
      sut.formatLine("my line", 0, blocks((1, true))) must equalIgnoreSpace(Text("my line"))
    }
    "format non-covered line" in {
      sut.formatLine("my line", 0, blocks((0, false))) must equalIgnoreSpace(<span class="non">my line</span>)
    }
    "format partially covered line" in {
      val result = Text("my ") ++ <span class="non">line</span>
      sut.formatLine("my line", 0, blocks((0, true), (3, false))) must equalIgnoreSpace(result)
      sut.formatLine("my line", 0, blocks((3, false))) must equalIgnoreSpace(result)
    }
    "format more partially covered line" in {
      val html = sut.formatLine("my somewhat longer line", 0, blocks((3, false), (12, true), (19, false)))
      html must equalIgnoreSpace(Text("my ") ++ <span class="non">somewhat </span> ++ Text("longer ") ++ <span class="non">line</span>)
    }
  }

  "Source file name cleanup" should {
    "strip base dir and cleanup duplicate /es" in {
      val sourceFilePathRelativeToBaseDir = "my/source/dir//package/and//Source.scala"
      val baseDir = new File("/baseDir")
      val sourceDir = new File(baseDir, "my/source/dir")
      val name = SourceFileHtmlReporter.cleanSourceName(sourceFilePathRelativeToBaseDir, baseDir, sourceDir)
      name mustEqual "package/and/Source.scala"
    }
  }
  "Header rendering" should {
    val sut = new SourceFileHtmlReporter("package/and/Source.scala", new CoverageData(Nil), List())
    "render file name" in {
      sut.sourceFileHeader mustEqual
        sut.zeroSpace ++ Text("package/") ++ sut.zeroSpace ++ Text("and/") ++ <span class="header">{ sut.zeroSpace ++ Text("Source.scala") }</span>
    }
  }

  def blocks(offsets: Tuple2[Int, Boolean]*): List[CoveredBlock] = {
    val name = Name("src", ClassTypes.Class, "pkg", "clazz", "project")
    val bs = for ((off,hit) <- offsets) yield {
      val b = new CoveredBlock(off.toString, name, off)
      if (hit) b.increment;
      b
    }
    bs.toList
  }
}