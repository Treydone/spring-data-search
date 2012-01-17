# Spring Search #

Spring Search is an abstraction on the search layer aiming help developpers to use a search engine. 

Spring Search provides a SearchTemplate with methods like the useful JdbcTemplate. Indeed, like the JdbcTemplate, you can add, update or extract the data directly from a QueryReponse with a QueryResponseExtractor or a DocMapper.
A short list of search engines that will be integrated:
* Solr
* ElasticSearch
* Compass (deprecated in favor of ElasticSearch?)
* Exalead
* Google Custom Search
* ... any idea?

Some future features:
* Abstract common DSL
* JPA cross storage (planned)
* Transaction management
* Exception translation

# Examples

// First build a search template, here for Solr:

```java
SolrServer embeddedSolrServer = new EmbeddedSolrServer(coreContainer, "default");
SearchOperations searchOperations = new SolrTemplate(embeddedSolrServer);
```

// Or, in the same way, build a template using Elastic Search:

```java
Node node = NodeBuilder.nodeBuilder().client(true).node();
Client client = node.client();
SearchOperations searchOperations = new ElasticSearchTemplate(client);
```

// And just do something like this:

// 1- add documents or beans

```java
searchOperations.add(document);
searchOperations.addBeans(bean1, bean2);
```

// 2- retrive document using a DocMapper or via a class using @Indexed annotation

```java
List<DummyBean> beans = searchOperations.query("id:1234", new DocMapper<DummyBean>() {
	@Override
	public DummyBean docMap(Document doc) {
		return new DummyBean((String) doc.get("id"), (Date) doc.get("last_modified"), (String) doc.get("name"));
	}
});


List<DummyBean> beans = searchOperations.query("id:1234", DummyBean.class);
```

// 3- have fun!

# Docs

Todo

# Artifacts

* Maven (not already deployed):

```xml
<dependency>
  <groupId>org.springframework.search</groupId>
  <artifactId>spring-search</artifactId>
  <version>${version}</version>
</dependency>
```
