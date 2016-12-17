/*
DemoApp.java
Simple demo of jSBML
*/

object JsbmlApp {
  import org.sbml.jsbml.SBMLReader
  // import org.sbml.jsbml.{ Unit => JsbmlUnit }
  import scala.collection.JavaConversions._

  def main(args: Array[String]): Unit = {
    val filename = if (args.length == 0)
      "ch07-mm-stoch.xml" else args(0)
    val reader = new SBMLReader
    val document = reader.readSBML(filename)
    val model = document.getModel
    println(model.getId + "\n" + model.getName)
    val listOfSpecies = model.getListOfSpecies
    val ns = model.getNumSpecies
    println(s"$ns Species:")
    listOfSpecies.iterator.foreach(species => {
      println("  " +
        species.getId + "\t" +
        species.getName + "\t" +
        species.getCompartment + "\t" +
        species.getInitialAmount)
    })
    val nr = model.getNumReactions
    println(s"$nr Reactions.")
  }

}

/* eof */

