/*
DemoApp.java

Simple demo of jSBML
 
*/


object JsbmlApp
{
    import org.sbml.jsbml.SBMLReader

    def main(args: Array[String]): Unit = {
	val filename="ch07-mm-stoch.xml"
	val reader = new SBMLReader()
	val document = reader.readSBML(filename)
	val model = document.getModel()
	val listOfSpecies = model.getListOfSpecies()
	
	(0 until model.getNumSpecies()).foreach(i => {
	    val species = listOfSpecies.get(i)
	    println(
			       species.getId() + "\t" +
			       species.getName() + "\t" +
			       species.getCompartment() + "\t" +
			       species.getInitialAmount()
			       )
	  }
       )
    }

}


/* eof */


