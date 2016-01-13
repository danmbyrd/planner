import java.io.File
import java.util
import org.jgap._
import org.jgap.event.EventManager
import org.jgap.impl._
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._


object Main {

  val populationSize = 500
  val fmt = DateTimeFormat.forPattern("yyyyMMdd");
  val startDate: DateTime = fmt.parseDateTime("20160101")

  def readInput(file: File): Map[Int, Destination] = {
    val places = Set(
      Destination(
        "Japan",
        Location(35.42F, 139.45F),
        Set(4, 11),
        14
      ),
      Destination(
        "New Zealand",
        Location(-36.55F, 174.46F),
        Set(2, 3),
        30
      ),
      Destination(
        "Australia",
        Location(-25.0F, 151.0F),
        Set(9, 10),
        30
      ),
      Destination(
        "Thailand",
        Location(16F, 101F),
        Set(11, 12, 1, 2),
        30
      ),
      Destination(
        "Tanzania",
        Location(-6F, 35F),
        Set(8, 9, 10),
        30
      )
    )
    (Range(0, places.size)).zip(places).toMap
  }

  def solve(destinations: Map[Int, Destination]): Genotype = {

    Configuration.reset()
    val fitnessFunction = { solution: Seq[Destination] =>
      val locations = solution.map(_.location)
      val (totalDistance, _) = locations.tail.foldLeft((0.0, locations.head)) { (distanceAndPrevLoc, nextLocation) =>
        val (distance, prevLoc) = distanceAndPrevLoc
        (distance + (prevLoc.distance(nextLocation)), nextLocation)
      }
      val visitingTimes = solution.foldLeft(startDate, Seq.empty[Interval]) { (soFar, dest) =>
        val (dateSoFar, intervals) = soFar
        val endDate = dateSoFar.plusDays(dest.duration)
        (endDate, intervals ++ Seq(new Interval(dateSoFar, endDate)))
      }
      val visitingScore = visitingTimes._2.zip(destinations).map { timeAndDest =>
        5
      }
      //println(s"Finding fitness of solution $solution with total distance $totalDistance")
      1 / totalDistance
    }

    // Start with a DefaultConfiguration, which comes setup with the
    // most common settings.
    // -------------------------------------------------------------
    val conf = new Configuration("", "") {
      val bestChromsSelector =
        new BestChromosomesSelector(this, 0.90d);
      bestChromsSelector.setDoubletteChromosomesAllowed(false);
      this.addNaturalSelector(bestChromsSelector, true);
      this.setRandomGenerator(new StockRandomGenerator());
      this.setMinimumPopSizePercent(100);
      this.setEventManager(new EventManager());
      this.setFitnessEvaluator(new DefaultFitnessEvaluator());
      this.setChromosomePool(new ChromosomePool());
      // These are different:
      // --------------------
      //thiss.addGeneticOperator(new GreedyCrossover(config));
      this.addGeneticOperator(new SwappingMutationOperator(this, 20));
    }

    // Set the fitness function we want to use, which is our
    // MinimizingMakeChangeFitnessFunction that we created earlier.
    // We construct it with the target amount of change provided
    // by the user.
    // ------------------------------------------------------------
    //val targetAmount = 5
    val myFunc = new FitnessFunction {
      override def evaluate(a_subject: IChromosome): Double = {
        val genes = a_subject.getGenes.map { case g: IntegerGene => g }
        val solution = genes.map { g => destinations(g.intValue())}
        fitnessFunction(solution)
      }
    }

    conf.setFitnessFunction(myFunc);

    // Now we need to tell the Configuration object how we want our
    // Chromosomes to be setup. We do that by actually creating a
    // sample Chromosome and then setting it on the Configuration
    // object. As mentioned earlier, we want our Chromosomes to
    // each have four genes, one for each of the coin types. We
    // want the values of those genes to be integers, which represent
    // how many coins of that type we have. We therefore use the
    // IntegerGene class to represent each of the genes. That class
    // also lets us specify a lower and upper bound, which we set
    // to sensible values for each coin type.
    // --------------------------------------------------------------
    val sampleGenes: Array[Gene] = destinations.keys.map { key =>
      val gene = new IntegerGene(conf, key, key)
      gene.setAllele(key)
      gene
    }.toArray

    val sampleChromosome = new Chromosome(conf, sampleGenes);

    conf.setSampleChromosome(sampleChromosome);

    // Finally, we need to tell the Configuration object how many
    // Chromosomes we want in our population. The more Chromosomes,
    // the larger the number of potential solutions (which is good
    // for finding the answer), but the longer it will take to evolve
    // the population each round. We'll set the population size to
    // populationSize here.
    // --------------------------------------------------------------
    conf.setPopulationSize(populationSize);

    val chromosomes = new Array[IChromosome](populationSize)

    val samplegenes: Array[Gene] = sampleChromosome.getGenes();
    for (i <- Range(0, populationSize)) {

      val genes = Range(0, samplegenes.length).map { k =>
        val newGene = samplegenes(k).newGene();
        newGene.setAllele(samplegenes(k).getAllele());
        newGene
      }
      scala.util.Random.shuffle(genes)
      val array = new Array[Gene](samplegenes.length)
      genes.copyToArray[Gene](array, 0, samplegenes.length)
      chromosomes(i) = new Chromosome(conf, array)
    }

    val population = new Genotype(conf, new Population(conf, chromosomes))

    for(i <- Range(1, 1000)) {
      population.evolve()
    }

    population
  }

  def chromoToDestinations(destinations: Map[Int,Destination], chromo: IChromosome): Seq[Destination] = {
    chromo.getGenes.map { case x: IntegerGene =>  destinations(x.intValue())}
  }

  def mostFitAndLeastFit(population: Genotype, destinations: Map[Int, Destination]) = {
    val fittestChromosome = population.getFittestChromosome
    val leastFit: IChromosome = population.getFittestChromosomes(populationSize).last.asInstanceOf[IChromosome]

    println(s"distance of fittest: ${1.0 / fittestChromosome.getFitnessValue}," +
      s" solution is: ${chromoToDestinations(destinations, fittestChromosome).map(_.name)}")

    println(s"distance of lest fit: ${1.0 / leastFit.getFitnessValue}," +
      s" solution is: ${chromoToDestinations(destinations, leastFit).map(_.name)}")

  }
}

case class Destination(name: String, location: Location, bestTime: Set[Int], duration: Int) {

}

case class Location(latitude: Float, longitude: Float) {
  def distance(other: Location) = {
    val thisRadLat = Math.toRadians(latitude)
    val otherRadLat = Math.toRadians(other.latitude)
    val longDifference = Math.toRadians(longitude - other.longitude)

    val dist = Math.sin(thisRadLat) * Math.sin(otherRadLat) + Math.cos(thisRadLat) * Math.cos(otherRadLat) * Math.cos(longDifference)
    val newDist = Math.acos(dist);
    val degreeDist = Math.toDegrees(newDist);
    degreeDist * 60 * 1.1515;
  }
}




