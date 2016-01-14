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

  val distanceWeight = 1.0
  val timingWeight = 2.0
  val populationSize = 500
  val prettyFmt = DateTimeFormat.forPattern("dd 'of' MMMM")
  val monthTime = DateTimeFormat.forPattern("MM").withDefaultYear(2016)
  val fmt = DateTimeFormat.forPattern("yyyyMMdd");
  val startDate: DateTime = fmt.parseDateTime("20160401")

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
      ),
      Destination(
        "China - East Coast",
        Location(31.14F, 121.30F),
        Set(10, 11),
        14
      ),
      Destination(
        "China - Hong Kong + South",
        Location(22.22F, 114.05F),
        Set(10, 11, 12),
        14
      ),
      Destination(
        "South Africa",
        Location(-31.0F, 23.0F),
        Set(5, 6, 7, 8, 9),
        30
      ),
      Destination(
        "France",
        Location(46F, 3F),
        Set(4, 5, 9),
        7
      ),
      Destination(
        "Germany",
        Location(51F, 10F),
        Set(5, 6, 7),
        7
      )
    )
    (Range(0, places.size)).zip(places).toMap
  }

  def fitnessFunction(destinations: Map[Int, Destination]) = { solution: Seq[Destination] =>

      val totalDistance = Destination.totalDistance(solution)

      val distanceScore = 1.0 / Math.sqrt(totalDistance)

      val visitingIntervals = Destination.visitingIntervals(solution)
      val visitingScore = (visitingIntervals.zipWithIndex.map { intervalAndIndex =>
        val (interval, index) = intervalAndIndex
        val destination = solution(index)
        destination.timingScore(interval)
      }.sum) / destinations.size

    (distanceWeight * distanceScore) + (visitingScore * timingWeight)
  }

  def solve(destinations: Map[Int, Destination], generations: Int = 1000): Genotype = {

    Configuration.reset()

    // Start with a DefaultConfiguration, which comes setup with the
    // most common settings.
    // -------------------------------------------------------------
    val conf = new Configuration("", "") {
      val bestChromsSelector =
        new BestChromosomesSelector(this, 1.0d);
      bestChromsSelector.setDoubletteChromosomesAllowed(false);
      this.addNaturalSelector(bestChromsSelector, true);
      this.setRandomGenerator(new StockRandomGenerator());
      this.setMinimumPopSizePercent(0);
      this.setEventManager(new EventManager());
      this.setFitnessEvaluator(new DefaultFitnessEvaluator());
      this.setChromosomePool(new ChromosomePool());
      // These are different:
      // --------------------
      this.addGeneticOperator(new GreedyCrossover(this) {
        override def distance(a_from: Object, a_to: Object): Double =  {
          val from: Destination = destinations(a_from.asInstanceOf[IntegerGene].intValue());
          val to: Destination = destinations(a_to.asInstanceOf[IntegerGene].intValue());
          from.location.distance(to.location)
        }

      });
      this.addGeneticOperator(new SwappingMutationOperator(this, 20));
    }

    // Set the fitness function we want to use, which is our
    // MinimizingMakeChangeFitnessFunction that we created earlier.
    // We construct it with the target amount of change provided
    // by the user.
    // ------------------------------------------------------------
    //val targetAmount = 5
    val myFunc = new FitnessFunction {
      val actualFunc = fitnessFunction(destinations)
      override def evaluate(a_subject: IChromosome): Double = {
        val genes = a_subject.getGenes.map { case g: IntegerGene => g }
        val solution = genes.map { g => destinations(g.intValue())}
        actualFunc(solution)
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

    for(i <- Range(1, generations)) {
      population.evolve()
    }

    population
  }

  def mostFitAndLeastFit(population: Genotype, destinations: Map[Int, Destination], startDate: DateTime) = {
    import Destination._
    val fittestChromosome = chromoToDestinations(destinations, population.getFittestChromosome)
    val leastFit = chromoToDestinations(
      destinations,
      population.getFittestChromosomes(populationSize).last.asInstanceOf[IChromosome]
    )

    def printSolution(solution: Seq[Destination], startDate: DateTime) = {
     val intervals = Destination.visitingIntervals(solution, startDate)
     val solutionAsString = solution.zip(intervals).map { s =>
       val (sol, int) = s
       s"${sol.name} - timing score: ${sol.timingScore(int)} (interval: ${intervalToString(int)})\n"
     }
     println(s"distance of solution: ${totalDistance(solution)}}," +
      s" solution is: ${solutionAsString}")

    }

    printSolution(fittestChromosome, startDate)
    printSolution(leastFit, startDate)

  }
}

case class Destination(name: String, location: Location, timingScoreF: Interval => Double, duration: Int) {
  def timingScore(interval: Interval) = timingScoreF(interval)
}

object Destination {
  def apply(name: String, location: Location, bestMonths: Set[Int], duration: Int): Destination = {
    Destination(name, location, LinearScoring(bestMonths), duration)
  }
  def totalDistance(destinations: Seq[Destination]) = {
    val locations = destinations.map(_.location)
    val (totalDistance, _) = locations.tail.foldLeft((0.0, locations.head)) { (distanceAndPrevLoc, nextLocation) =>
        val (distance, prevLoc) = distanceAndPrevLoc
        (distance + (prevLoc.distance(nextLocation)), nextLocation)
    }
    totalDistance
  }

  def visitingIntervals(destinations: Seq[Destination], startDate: DateTime = Main.startDate) = {
    destinations.foldLeft(startDate, Seq.empty[Interval]) { (soFar, dest) =>
      val (dateSoFar, intervals) = soFar
      val endDate = dateSoFar.plusDays(dest.duration)
      (endDate, intervals ++ Seq(new Interval(dateSoFar, endDate)))
    }._2
  }

  def prettyIntervals(destinations: Seq[Destination], startDate: DateTime = Main.startDate) = {
    val intervals = visitingIntervals(destinations, startDate)
    intervals.map(intervalToString)
  }

  def intervalToString(i: Interval) = {
    s"${i.getStart.toString(Main.prettyFmt)} - ${i.getEnd.toString(Main.prettyFmt)}"
  }

  def chromoToDestinations(destinations: Map[Int,Destination], chromo: IChromosome): Seq[Destination] = {
    chromo.getGenes.map { case x: IntegerGene =>  destinations(x.intValue())}
  }

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

case class LinearScoring(bestMonths: Set[Int]) extends Function[Interval, Double] {

  def scoreDay(month: Int) = {
    val distances = bestMonths.map { best => Math.abs(best - month) }
    (12 - distances.min) / 12.0
  }

  /*val bestMonthsIntervals = bestMonths.map { month =>
      val endDate = (month % 12) + 1
      val endDateWithYear = if (endDate == 1) {
        Main.fmt.parseDateTime("20170101")
      } else {
        Main.monthTime.parseDateTime(endDate.toString)
      }
      new Interval(
        Main.monthTime.parseDateTime(month.toString),
        endDateWithYear
      )
    }*/

  override def apply(interval: Interval): Double = {

    val daysOfEachMonth = {
      var pointer = interval.getStart
      val days = new ArrayBuffer[Int]
      val end = interval.getEnd
      while (pointer.isBefore(end)) {
        days += pointer.getMonthOfYear
        pointer = pointer.plusDays(1)
      }
      days.toSeq
    }

    val scoreUnnormal = daysOfEachMonth.map { day => scoreDay(day)}.sum
    Math.pow(scoreUnnormal / daysOfEachMonth.size.toDouble, 2)
    /*val overlaps = bestMonthsIntervals.map { bestTime => interval.overlap(bestTime)}
    val overlappingDays = overlaps.filter { o => Option(o).isDefined }.map { _.toDuration.getStandardDays }.sum
    if (overlappingDays == 0.0) {
      0.0
    } else {
      overlappingDays.toDouble / interval.toDuration.getStandardDays.toDouble
    } */

  }
}




