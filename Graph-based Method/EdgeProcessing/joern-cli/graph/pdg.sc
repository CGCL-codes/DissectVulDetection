/* 
每个函数生成的ast,cfg,pdg放在一个文件中
*/

import gremlin.scala.{Edge, GremlinScala}

import io.shiftleft.codepropertygraph.generated.EdgeTypes

import scala.collection.mutable

import java.io.{PrintWriter, File => JFile}
import java.io.{File}

type pEdgeEntry = (AnyRef, AnyRef,Int)
type pVertexEntry = (AnyRef, String)
//type pdg = (Option[String], List[pEdgeEntry], List[pVertexEntry])

//type r = (List[ast],List[cfg],List[pdg])
type r = (Option[String], List[pEdgeEntry], List[pVertexEntry])


private def pdgFromEdges(edges: GremlinScala[Edge]): (List[pEdgeEntry], List[pVertexEntry]) = {
  val filteredEdges = edges.filter(edge => edge.hasLabel(EdgeTypes.REACHING_DEF, EdgeTypes.CDG)).dedup.l

  val (edgeResult, vertexResult) =
    filteredEdges.foldLeft((mutable.Set.empty[pEdgeEntry], mutable.Set.empty[pVertexEntry])) {
      case ((edgeList, vertexList), edge) =>
        val edgeEntry = (edge.inVertex().id, edge.outVertex().id,2)
        val inVertexEntry = (edge.inVertex().id, edge.inVertex().property("CODE").orElse(""))
        val outVertexEntry = (edge.outVertex().id, edge.outVertex().property("CODE").orElse(""))

        (edgeList += edgeEntry, vertexList ++= Set(inVertexEntry, outVertexEntry))
    }

  (edgeResult.toList, vertexResult.toList)
}


//type r = (Option[String], List[aEdgeEntry], List[aVertexEntry], List[cEdgeEntry], List[cVertexEntry],List[pEdgeEntry], List[pVertexEntry])


def result(methodRegex: String = ""): List[r] = {
  if (methodRegex.isEmpty) {
    val (pedgeEntries, pvertexEntries) = pdgFromEdges(cpg.scalaGraph.E())
    List((None, pedgeEntries, pvertexEntries))
  } else {
    cpg.method(methodRegex).l.map { method =>
      val methodFile = method.location.filename+"-"+method.name
      val (pedgeEntries, pvertexEntries) = pdgFromEdges(method.asScala.out().flatMap(_.asScala.outE()))

      (Some(methodFile), pedgeEntries, pvertexEntries)
    }
  }
}


@main def main()= {

  var item = 0
  val list = result(".*")
  println(list.length)
  //Please modify the path of the result
  val dirPath = "raw_result//good_pdg"
  val resultPath = new File(dirPath)
  resultPath.mkdirs()

  for (item <- list){
        var filename=BigInt(100, scala.util.Random).toString(36)
	val writer = new PrintWriter(new JFile(dirPath+"//"+filename+".txt"))
	writer.println(item)
        writer.close()
  }
  
}
