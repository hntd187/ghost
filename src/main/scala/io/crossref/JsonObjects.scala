package io.crossref

object JsonObjects {

  case class DateParts(dateParts: Seq[Seq[Int]], dateTime: Option[String], timestamp: Option[Long])

  case class ContentDomain(domain: Seq[String], crossmarkRestriction: Boolean)

  case class Authors(given: Option[String], family: Option[String], affiliation: Seq[String])

  case class PublishedOnline(dateParts: DateParts)

  case class Deposited(dateParts: DateParts)

  case class Issued(dateParts: DateParts)

  case class Link(URL: String, contentType: String, contentVersion: String, intendedApplication: String)

  case class ISSNType(value: String, `type`: String)

  case class Relation()

  case class Publication(indexed: DateParts,
                         referenceCount: Int,
                         publisher: String,
                         issue: Option[String],
                         contentDomain: ContentDomain,
                         shortContainerTitle: Seq[String],
                         DOI: String,
                         `type`: String,
                         created: DateParts,
                         page: Option[String],
                         source: String,
                         isReferencedByCount: Int,
                         title: Seq[String],
                         prefix: String,
                         volume: Option[String],
                         author: Seq[Authors],
                         member: String,
                         publishedOnline: PublishedOnline,
                         containerTitle: Seq[String],
                         originalTitle: Seq[String],
                         link: Seq[Link],
                         deposited: Deposited,
                         score: Int,
                         subtitle: Seq[String],
                         shortTitle: Seq[String],
                         issued: Issued,
                         referencesCount: Int,
                         alternativeId: Seq[String],
                         URL: String,
                         relation: Relation,
                         ISSN: Seq[String],
                         issnType: Seq[ISSNType],
                         subject: Seq[String])

  case class Facets()

  case class Query(startIndex: Int, searchTerms: String)

  case class Items(facets: Facets, totalResults: Long, items: Seq[Publication], itemsPerPage: Int, query: Query, nextCursor: Option[String])

  case class Response[M](status: String, messageType: String, messageVersion: String, message: M)

}
