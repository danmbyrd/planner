import org.joda.time.Interval

/**
 * Created by dan on 1/14/16.
 */
object Destinations {

  val dests = {
    val places = Set(Destination(
      "Japan",
      Location(35.42F, 139.45F),
      Set(4, 11),
      18,
      false
    ),
    Destination(
      "New Zealand",
      Location(-36.55F, 174.46F),
      Set(2, 3),
      21,
      false
    ),
    Destination(
      "Australia",
      Location(-25.0F, 151.0F),
      Set(9, 10),
      16,
      false
    ),
    Destination(
      "Thailand",
      Location(16F, 101F),
      Set(11, 12, 1, 2),
      18,
      false
    ),
    Destination(
      "Tanzania",
      Location(-6F, 35F),
      Set(8, 9, 10),
      10,
      false
    ),
    Destination(
      "China - East Coast",
      Location(31.14F, 121.30F),
      Set(10, 11),
      14,
      false
    ),
    Destination(
      "China - Hong Kong + South",
      Location(22.22F, 114.05F),
      Set(10, 11, 12),
      7,
      false
    ),
    Destination(
      "South Korea ",
      Location(37.45F, 127.3F),
      Set(4, 5, 9, 10, 11),
      7,
      false
    ),
    Destination(
      "Indonesia - Bali",
      Location(-8.18F, 115.06F),
      Set(4, 5, 9),
      9,
      false
    ),
    Destination(
      "India",
      Location(28.3F, 77.06F),
      Set(11, 12, 1, 2),
      14,
      false
    ),
    Destination(
      "South Africa",
      Location(-31.0F, 23.0F),
      Set(5, 6, 7, 8, 9),
      10,
      false
    ),
    Destination(
      "France",
      Location(46F, 3F),
      Set(4, 5, 9),
      14,
      true
    ),
    Destination(
      "Germany",
      Location(51F, 10F),
      ("2016-09-16", "2016-10-01"),
      14,
      true
    ),
    Destination(
      "Spain",
      Location(41F, -3F),
      Set(5, 6, 9, 10),
      14,
      true
    ),
    Destination(
      "Belgium + Netherlands",
      Location(51.2F, 5F),
      Set(3, 4, 5, 9, 10),
      4,
      true
    ),
    Destination(
      "Croatia",
      Location(44.5F, 15.2F),
      Set(4, 5, 6, 9),
      14,
      false
    ),
    Destination(
      "Italy",
      Location(45F, 11F),
      Set(4, 5, 6, 9, 10),
      14,
      true
    ),
    Destination(
      "Hungary",
      Location(47.3F, 19F),
      Set(3, 4, 5, 9, 10, 11),
      7,
      true
    ),
    Destination(
      "Czech Republic",
      Location(50F, 15F),
      Set(3, 4, 5, 9, 10, 11),
      4,
      true
    ),
    Destination(
      "United Kingdom",
      Location(51.3F, 0F),
      Set(4, 5, 6, 7, 8, 9),
      14,
      false
    ),
    Destination(
      "Sweden",
      Location(59.3F, 18F),
      Set(6, 7, 8),
      7,
      true
    ),
    Destination(
      "Denmark",
      Location(40.1F, 12F),
      Set(6, 7, 8),
      4,
      true
    ),
    Destination(
      "Vietnam",
      Location(10F, 106F),
      Set(1, 2, 3, 4, 12),
      14,
      false
    ),
    Destination(
      "Greece",
      Location(39F, 21F),
      Set(4, 5, 6, 9, 10),
      7,
      true
    ),
    Destination(
      "Iceland",
      Location(64F, -19F),
      Set(6, 7, 8),
      7,
      true
    ),
    Destination(
      "Austria + Switzerland",
      Location(47F, 12F),
      Set(4, 5, 9, 10, 11),
      7,
      true
    ),
    Destination(
      "Portugal",
      Location(39F, -8F),
      Set(3, 4, 5, 9, 10),
      5,
      true
    ),
    Destination(
      "Ireland",
      Location(53F, -8F),
      Set(5, 6, 9),
      4,
      false
    )
  )
  (Range(0, places.size)).zip(places).toMap
}
}
