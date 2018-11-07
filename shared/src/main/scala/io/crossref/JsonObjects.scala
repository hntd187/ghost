package io.crossref
import io.circe.Json

object JsonObjects {

  case class DateParts(`date-parts`: Seq[Seq[Json]], `date-time`: String, timestamp: Long)

  case class PartialDate(`date-parts`: Seq[Seq[Json]])

  case class ContentDomain(domain: Seq[String], `crossmark-restriction`: Boolean)

  case class Affiliation(name: String)

  case class Authors(given: Option[String], family: Option[String], affiliation: Seq[Affiliation])

  case class PublishedOnline(dateParts: DateParts)

  case class Deposited(dateParts: DateParts)

  case class Issued(dateParts: DateParts)

  case class Link(URL: String, `content-type`: String, `content-version`: String, `intended-application`: String)

  case class ISSNType(value: String, `type`: String)

  case class Relation(`id-type`: String, id: String, `asserted-by`: String)

  case class ClinicalTrialNumber(`clinical-trial-number`: String, registry: String, `type`: Option[String])

  case class Contributor(family: Option[String],
                         given: Option[String],
                         ORCID: Option[String],
                         `authenticated-orcid`: Option[Boolean],
                         affiliation: Seq[Affiliation])

  case class Update(updated: PartialDate, DOI: String, `type`: String, label: Option[String])

  case class ExplanationURL(URL: String)

  case class Assertion(name: Option[String],
                       value: Option[String],
                       URL: Option[String],
                       explanation: Option[ExplanationURL],
                       label: Option[String],
                       order: Option[Int],
                       group: Option[AssertionGroup])

  case class AssertionGroup(name: Option[String], label: Option[String])

  case class License(`content-version`: String, `delay-in-days`: Int, start: Option[DateParts], URL: Option[String])

  case class ResourceLink(`intended-application`: String, `content-version`: String, URL: String, `content-type`: Option[String])

  case class Funder(name: Option[String], DOI: Option[String], award: Seq[String], `doi-asserted-by`: Option[String])

  case class JournalIssue(issue: String)

  case class Reference(key: Option[String],
                       author: Option[String],
                       issue: Option[String],
                       edition: Option[String],
                       component: Option[String],
                       `standard-designator`: Option[String],
                       `standards-body`: Option[String],
                       unstructured: Option[String],
                       `article-title`: Option[String],
                       `series-title`: Option[String],
                       `volume-title`: Option[String],
                       `journal-issue`: Option[JournalIssue],
                       ISSN: Option[String],
                       `issn-type`: Option[String],
                       ISBN: Option[String],
                       `isbn-type`: Option[String],
                       volume: Option[String],
                       `first-page`: Option[String],
                       year: Option[String],
                       `journal-title`: Option[String],
                       DOI: Option[String],
                       `doi-asserted-by`: Option[String])

  case class Publication(publisher: Option[String],
                         title: Option[Seq[String]],
                         `original-title`: Option[Seq[String]],
                         `short-title`: Option[Seq[String]],
                         `abstract`: Option[String],
                         `is-referenced-by-count`: Int,
                         source: Option[String],
                         prefix: Option[String],
                         DOI: Option[String],
                         URL: Option[String],
                         member: Option[String],
                         `type`: Option[String],
                         created: DateParts,
                         deposited: DateParts,
                         indexed: DateParts,
                         issued: PartialDate,
                         posted: Option[PartialDate],
                         accepted: Option[PartialDate],
                         subtitle: Option[Seq[String]],
                         `container-title`: Option[Seq[String]],
                         `short-container-title`: Option[Seq[String]],
                         `group-title`: Option[String],
                         issue: Option[String],
                         volume: Option[String],
                         page: Option[String],
                         `article-number`: Option[String],
                         `published-print`: Option[PartialDate],
                         `published-online`: Option[PartialDate],
                         subject: Option[Seq[String]],
                         ISSN: Option[Seq[String]],
                         `issn-type`: Option[Seq[ISSNType]],
                         ISBN: Option[Seq[String]],
                         archive: Option[Seq[String]],
                         license: Option[Seq[License]],
                         funder: Option[Seq[Funder]],
                         assertion: Option[Seq[Assertion]],
                         author: Option[Seq[Authors]],
                         editor: Option[Seq[Contributor]],
                         chair: Option[Seq[Contributor]],
                         translator: Option[Seq[Contributor]],
                         `update-to`: Option[Seq[Update]],
                         `update-policy`: Option[String],
                         link: Option[Seq[Link]],
                         `clinical-trial-number`: Option[Seq[ClinicalTrialNumber]],
                         `alternative-id`: Option[Seq[String]],
                         reference: Option[Seq[Reference]],
//                         `content-domain`: Int = 0,
                         //relation: Option[Map[String, Seq[Any]]],
                         score: Int,
                         `reference-count`: Int,
                         `references-count`: Int)

  case class Query(`start-index`: Int, `search-terms`: Option[String])

  case class Items(facets: Option[Map[String, String]],
                   `total-results`: Option[Long],
                   items: Seq[Publication],
                   `items-per-page`: Int,
                   query: Query,
                   `next-cursor`: Option[String])

  case class Response[M <: Product](status: String, messageType: String, messageVersion: String, message: M)

  case class ItemResponse(status: String, message: Items, `message-type`: String, `message-version`: String)
}