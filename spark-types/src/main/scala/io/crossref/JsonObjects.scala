package io.crossref

case class DateParts(`date-parts`: Array[Array[Long]], `date-time`: String, timestamp: Long)

case class PartialDate(`date-parts`: Array[Array[Long]])

case class ContentDomain(domain: Seq[String], `crossmark-restriction`: Boolean)

case class Affiliation(name: String)

case class Authors(given: String, family: String, affiliation: Seq[Affiliation])

case class PublishedOnline(dateParts: DateParts)

case class Deposited(dateParts: DateParts)

case class Issued(dateParts: DateParts)

case class Link(URL: String, `content-type`: String, `content-version`: String, `intended-application`: String)

case class ISSNType(value: String, `type`: String)

case class Relation(`id-type`: String, id: String, `asserted-by`: String)

case class ClinicalTrialNumber(`clinical-trial-number`: String, registry: String, `type`: String)

case class Contributor(family: String, given: String)

case class Update(updated: PartialDate, DOI: String, `type`: String, label: String)

case class ExplanationURL(URL: String)

case class Assertion(name: String,
                     value: String,
                     URL: String,
                     explanation: ExplanationURL,
                     label: String,
                     order: Long,
                     group: AssertionGroup)

case class AssertionGroup(name: String, label: String)

case class License(`content-version`: String, `delay-in-days`: Long, start: DateParts, URL: String)

case class ResourceLink(`intended-application`: String, `content-version`: String, URL: String, `content-type`: String)

case class Funder(name: String, DOI: String, award: Seq[String], `doi-asserted-by`: String)

case class JournalIssue(issue: String)

case class Reference(key: String,
                     author: String,
                     issue: String,
                     edition: String,
                     unstructured: String,
                     `article-title`: String,
                     `series-title`: String,
                     `volume-title`: String,
                     ISBN: String,
                     `isbn-type`: String,
                     volume: String,
                     `first-page`: String,
                     year: String,
                     `journal-title`: String,
                     DOI: String,
                     `doi-asserted-by`: String)

case class Publication(publisher: String,
                       title: Seq[String],
                       abs: String,
                       `is-referenced-by-count`: Long,
                       source: String,
                       prefix: String,
                       DOI: String,
                       URL: String,
                       member: String,
                       `type`: String,
                       created: DateParts,
                       deposited: DateParts,
                       indexed: DateParts,
                       issued: PartialDate,
                       subtitle: Seq[String],
                       `container-title`: Seq[String],
                       `short-container-title`: Seq[String],
                       issue: String,
                       volume: String,
                       page: String,
                       `article-number`: String,
                       `published-print`: PartialDate,
                       `published-online`: PartialDate,
                       subject: Seq[String],
                       ISSN: Seq[String],
                       `issn-type`: Seq[ISSNType],
                       archive: Seq[String],
                       license: Seq[License],
                       funder: Seq[Funder],
                       author: Seq[Authors],
                       editor: Seq[Contributor],
                       `update-to`: Seq[Update],
                       `update-policy`: String,
                       link: Seq[Link],
                       `alternative-id`: Seq[String],
                       reference: Seq[Reference],
                       `content-domain`: ContentDomain,
                       score: Double,
                       `reference-count`: Long,
                       `references-count`: Long)
