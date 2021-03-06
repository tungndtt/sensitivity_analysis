# Sensitivity Analysis

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white) ![Postgresql](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

This is the implementation, which is served for sensitive analysis in [process mining](https://en.wikipedia.org/wiki/Process_mining#:~:text=Process%20mining%20is%20a%20family,data%20into%20insights%20and%20actions.)

## Purpose

__CS Bachelor Thesis__ <br />
@University __TU Darmstadt__ <br />
@Advisor __Dr.Alexander Seeliger__ <br />
@Student __Tung Nguyen__

## Usage && Dependencies

1. [Postgresql version 13.1](https://www.postgresql.org/)
2. [Java JDK 13](https://www.java.com/en/)
3. [Apache Maven 3.6.3](https://maven.apache.org/)
4. [Open XES](https://www.xes-standard.org/openxes/start)
5. [JDBC Postgresql Driver](https://jdbc.postgresql.org/download.html)

## Structure

/java
> /analysis
>> /metric
>>> metric implementation classes

>> /variation
>>> variation algorithm implementation classes

> /condition
>> /value
>>> value for attribute queries (nummerical, date, string...)
>>> 
>> conditions for queries (and, or, not, in...)

> /main
>> main application

> /query
>> /analysis
>>> Implementation of some queries for analysis
>>> 
>> /common
>>> Implementation of some common queries
>>>
>> /model
>>> Models to map into java object from retrived result-set

> /xlog
>> Read and import the event log

## Project class hierarchy
![hierarchy](https://github.com/tungndtt/sensitivity_analysis/blob/master/project_hierarchy.PNG)

## Support formats

Support event log in format .xes.gz or .xes

## Event Log Requirements

In event log, events in traces should contain following attributes: 

* concept:name
* time:timestamp
* org:resource
* org:group
