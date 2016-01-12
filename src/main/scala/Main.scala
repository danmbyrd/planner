import java.io.File
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object Main {

  val fmt = DateTimeFormat.forPattern("yyyyMMdd");
  val startDate: DateTime = fmt.parseDateTime("20160401")

  def readInput(file: File): Set[Destination] = {
   Set(
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
      Set(11,12,1,2),
      30
    ),
    Destination(
      "Tanzania",
      Location(- 6F, 35F),
      Set(8, 9, 10),
      30
    )
   )
  }

  def solve(destinations: Set[Destination]): Seq[Destination] = {

    val fitnessFunction = { solution: Seq[Destination] =>
      val locations = solution.map(_.location)
      val totalDistance = locations.tail.foldLeft((0.0, locations.head)) { (distanceAndPrevLoc, nextLocation) =>
        val (distance, prevLoc) = distanceAndPrevLoc
        (distance + (prevLoc.distance(nextLocation)), nextLocation)
      }
      val visitingTimes =
    }
    Seq()
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



