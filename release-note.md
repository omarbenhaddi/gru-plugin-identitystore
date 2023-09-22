# Release Note

This release note is quite detailed but please refer to [Official documentation](https://lutece.paris.fr/support/wiki/gru-library-identitystore-v3.html) for further information.

Versions:
* [3.0.0](#300)
* [3.1.0](#310)

## 3.0.0
This major version introduces new features. V1 and V2 are kept in corresponding packages. A new V3 package contains updated and new features.

### Identities referential
#### Client applications
[Client applications](src/java/fr/paris/lutece/plugins/identitystore/business/application/ClientApplication.java)  now have a client code and an application code. An application is a group of clients, which means that several clients application can belong to a common application. The API request will use client code to identify the caller.

#### Certification processus 
User can define a list of available [certification processus](src/java/fr/paris/lutece/plugins/identitystore/business/referentiel/RefAttributeCertificationProcessus.java) that defines the [certification level](src/java/fr/paris/lutece/plugins/identitystore/business/referentiel/RefCertificationLevel.java) requirements on [attributes](src/java/fr/paris/lutece/plugins/identitystore/business/attribute/AttributeKey.java). When creating an [identity](src/java/fr/paris/lutece/plugins/identitystore/business/identity/Identity.java), the attributes must be certified according to this configuration.

#### Service contracts
User can define a [service contract](src/java/fr/paris/lutece/plugins/identitystore/business/contract/ServiceContract.java) for each declared [client application](src/java/fr/paris/lutece/plugins/identitystore/business/application/ClientApplication.java) that may call the identity store API. A contract defines :
* the application authorizations on APIs : creation, update, merge, deletion, import, etc...
* the application rights on attributes: search, read, write, and mandatory for identity creation  
* the application requirements on identity quality for each attribute

Every action performed on an identity or attribute is checked against the service contract of the calling application. It means that if a client calls an API is not allowed to, or tries to modify an attribute is not allowed to, the request is refused with a status that explains the reason.

An application can have several service contracts but only one is active at a given date.

### API
All existing APIs have been upgraded to use the service contract definition as specified in the previous chapter.

A set of new API have been introduced.

#### Referential
A new API have been created to access [referential configuration](src/java/fr/paris/lutece/plugins/identitystore/v3/web/rs/ReferentielRestService.java):
* **GET /referential/processus** read defined certification levels list <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **GET /referential/level** read defined certification processus list <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>

#### Client applications
A new API have been created to access [clients configuration](src/java/fr/paris/lutece/plugins/identitystore/v3/web/rs/ClientRestService.java):
* **GET /clients/{application_code}** read defined client application list for a given application code <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **GET /client/{client_code}** read defined client application for a given client code <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **POST /client** create a new client application <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **PUT /client/{client_code}** update an existing client application <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>

#### Service Contract
A new API have been created to access [service contracts configuration](src/java/fr/paris/lutece/plugins/identitystore/v3/web/rs/ServiceContractRestService.java):
* **GET /contracts** read defined service contracts list for all client applications <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **GET /contracts/{target_client_code}** read defined service contracts list of a given client application <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **GET /contract/active/{target_client_code}** read defined active service contracts of a given client application <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **GET /contract/{service_contract_id}** read defined service contracts by its internal ID <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **POST /contract** create service contract definition for a given client application (creation of a new service contract) <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **PUT /contract/{service_contract_id}** update service contract definition for a given internal ID (update of an existing service contract) <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **PUT /contract/{service_contract_id}/{end_date}** service contract definition for a given internal ID (close an existing service contract) <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>

#### Identity
The existing [identity api](src/java/fr/paris/lutece/plugins/identitystore/v3/web/rs/IdentityStoreRestService.java) have been upgraded with new services
* **GET /identity/{customer_id}** get an identity for a given customer ID <a target="_blank"><img alt='UPDATED' src='https://img.shields.io/badge/UPDATED-100000?style=flat&logo=UPDATED&logoColor=FF6247&labelColor=FF6247&color=FF6247'/></a>
* **POST /identity/search** search for identities according to given criteria <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **POST /identity** create a new identity <a target="_blank"><img alt='UPDATED' src='https://img.shields.io/badge/UPDATED-100000?style=flat&logo=UPDATED&logoColor=FF6247&labelColor=FF6247&color=FF6247'/></a>
* **PUT /identity/{customer_id}** update an existing identity identified by its customer ID <a target="_blank"><img alt='UPDATED' src='https://img.shields.io/badge/UPDATED-100000?style=flat&logo=UPDATED&logoColor=FF6247&labelColor=FF6247&color=FF6247'/></a>
* **POST /identity/merge** merge two identities according to payload criteria <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>

### Indexing
Identities are now indexed into an ElasticSearch cluster. So the search feature of Identity API is built on ES search.

## 3.1.0

This minor version introduces a set of new APIs, a refactoring of Identity DTOs and a new capability for resolving duplicate identities. This new feature is available through [module-identitystore-quality](https://github.com/lutece-secteur-public/gru-module-identitystore-quality).

### Duplication management
module-identitystore-quality comes with a set of features that enables the user to identify and resolves identities duplications.

#### Rules
The user can configure a set of [duplicate rules](src/java/fr/paris/lutece/plugins/identitystore/business/rules/duplicate/DuplicateRule.java) that define criteria for duplicate identification:

* A list of working attributes
* The number of attributes that must be filled in the identity to perform the research
* The number of attributes that must be equal
* The specific treatment of other attributes
  * absent
  * different
  * approximately equal (using [Levenshtein distance](https://en.wikipedia.org/wiki/Levenshtein_distance))
  
#### Daemons
The identity duplicates daemon gets all defined rules and select all identities in the database that match the rule requirements. And then performs for each identity search (in ElasticSearch repository) for duplicates according to the definition of the rule.

If results are found the identity is marked as suspicious and associated with the rule code.

#### APIs
The identity store does not provide any resolving interface. This feature is included in other plugin. But it provides all needed APIs (see below)

### API

#### Quality
This API is provided by module [identity-quality](https://github.com/lutece-secteur-public/gru-module-identitystore-quality)

A new API have been created to access [suspicious identities' management](src/java/fr/paris/lutece/plugins/identitystore/modules/quality/rs/SuspiciousIdentityRest.java):
* **POST /quality/suspicions/search** search for suspicious identities according to given criteria <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **POST /quality/suspicions** register a suspicion if not already reported <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **POST /quality/exclusion** mark two identities as excluded from duplication suspicions <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **POST /quality/unexclude** remove two identities from duplication exclusions <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **GET /quality/rules?priority={priority}** get list of duplicate rules (from optional priority if specified, full if not) <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **GET /quality/duplicate/{customer_id}?code={rule_code}** get the list of identities that are duplicates of the provided customer_id's identity, according to the provided rule ID <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **POST /quality/lock** lock or unlock (according to payload) a suspicion that is being resolved to avoid concurrent accesses <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>

#### History
A new API have been created to access [identity history](src/java/fr/paris/lutece/plugins/identitystore/v3/web/rs/HistoryRestService.java):
* **GET /history/{customer_id}** read the identity history for a given customer ID <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **POST /history/search** perform research into identity history with a set of criteria <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>

#### Identity
The existing [identity api](src/java/fr/paris/lutece/plugins/identitystore/v3/web/rs/IdentityStoreRestService.java) have been upgraded with new services
* **POST /identity/merge** merge two identities according to payload criteria, the payload specifies attributes to keep from secondary identity <a target="_blank"><img alt='UPDATED' src='https://img.shields.io/badge/UPDATED-100000?style=flat&logo=UPDATED&logoColor=FF6247&labelColor=FF6247&color=FF6247'/></a>
* **POST /identity/unmerge** cancel identities merge according to payload criteria <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **DELETE /identity/{customer_id}** request an identity deletion <a target="_blank"><img alt='UPDATED' src='https://img.shields.io/badge/UPDATED-100000?style=flat&logo=UPDATED&logoColor=FF6247&labelColor=FF6247&color=FF6247'/></a>
* **GET /identity/updated** gets all modified identity CUIDs and their modification time within the last given number of days <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>
* **PUT /identity/uncertify/{customer_id}** de-certifies all attributes of an existing Identity identified by its customer ID <a target="_blank"><img alt='NEW' src='https://img.shields.io/badge/NEW-100000?style=flat&logo=NEW&logoColor=00D756&labelColor=00D756&color=00D756'/></a>

### DTO
#### Identities and attributes

As there was too much DTOs representing Identities in different contexts (QualifiedIdentity, Identity, Identities, CertifiedAttribute, ...), all have been merged into one IdentityDto containing AttributeDto into common package.

It means that now, every identity with attributes in a request or a response is an IdentityDto with AttributeDto. 

Below the detailed modifications:

* **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.Identity** merged to **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto**
* **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity** merged to **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto**
* **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.AttributeDto** merged to **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto**
* **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute** merged to **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto**
* **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeRequirement** renamed to **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeRequirementDto**
* **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeRight** renamed to **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeRightDto**
* **fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.CertificationProcessus** renamed **to fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.CertificationProcessusDto**

Properties have been organized in dedicated objects according to their context, so IdentityDto is now composed of:
* **connectionId** - the connection ID
* **customerId** - the customer ID
* **externalCustomerId** - optional, used for upcoming import feature
* **monParisActive** - true if connected
* **creationDate** - date of identity creation
* **lastUpdateDate** - date of the last modification
* **suspicious** - true if hte identity is a potential duplicate
* **list of AttributeDto** - list of attributes
* **QualityDefinition** - optional object that holds the quality properties of the identity
* **ExpirationDefinition** - optional object that holds the expiration properties of the identity
* **MergeDefinition** - optional object that holds the merge properties of the identity
* **IdentityDuplicateDefinition** - optional object that holds the duplication properties of the identity

AttributeDto is now composed of:
* **key** - the attribute key
* **value** - the attribute value
* **type** - the attribute type
* **certificationLevel** - certification level
* **certifier** - code of certification processus
* **certificationDate** - certification date

#### Response status
Like identities, there was too much response status type (one for each context, again). So all have been merged to ResponseStatus which is composed of:

* **httpCode** - http return code respecting [REST recommendations](https://www.ietf.org/rfc/rfc2616.txt)
* **type** - the name of the status corresponding to the return code (OK, SUCCESS, CONFLICT, FAILURE, etc..)
* **message** - optional, the message describing the execution of the request
* **messageKey** - a message key that allows user to get the default message provided in client library, or a local custom one
* **attributeStatuses** - particular case of a response to an Identity related request, holds the status of each attribute treatment

#### Origin / Author in API requests
Origin object has been removed from every request payload and is now provided for all APIs in the header of the request as two parameters:
* **author_name** - the name of the author
* **author_type** - the type of the author which must match one of the enum AuthorType values

Client code is now mandatory in every request header:
* **client_code** - code of the client calling the API, must match one of the defined Client Applications in identity store referential