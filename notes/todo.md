# Todos

Here we list important todos

## General

### Code Generation
- [x] Create Lenses and Pseudo Lenses via Gradle Plugin 
- [x] Create DB Migrations via Gradle Plugin

### Routing (Frontend)
- [x] common layouts under specific routes
- [x] conditional routing / control access efficiently 

### Code Organization
1. This is an ongoing task. There are many parts of the code, which are to be extracted in separate modules / projects:
   - Authorization / Authentication
   - User Management
   - Permission Api
   - Frontend Components
   - ...
2. Define typealiases for entities, domain types, and api types (serializable types)
   - [x] authentication
   - [x] auctions / bid
   - [ ] usermanagement
     - [x] entities
   - [ ] rights and roles
     - [x] entities

### Style
- [x] Adjust ModalLayer, so that it is capable of showing messages 
  - on the bottom of the page
  - at the center of the page
  - elsewhere

- [ ] Improve style of modals
- [ ] Improve style of DatePicker

### I18N
- [ ] Load localized messages on demand / on the fly
- [x] Setup DSL for Lang
- [x] Establish merge algorithm for Lang

## Features
### Auctions
- [ ] Create / manage Auctions
  - [ ] Create: 
    - [ ] Frontend: // all components wip are there but WIP
      - [x] AuctionPage
      - [x] AuctionList
      - [x] AuctionItemList
      - [x] Modal (Create)
  - [ ] Api Calls: 
    - [ ] Auctions
      - [x] Read Auctions  
      - [ ] Need to adjust type !!! 
        - [x] date added
        - [ ] What about adding a state to the Auction type 
          - [ ] OPEN, STARTED, STOPPED, FROZEN
      - [x] Create Auctions
      - [ ] Update Auctions
      - [x] Delete Auctions
      - [ ] Evaluation
      - [ ] Import Bidders
    - [ ] BidRounds
      - [ ] Evaluation
      - [ ] Create
      - [ ] Read
      - [ ] Update
      - [ ] Delete
    - [ ] Bidders
      - [ ] Create
      - [ ] Read
      - [ ] Update
      - [ ] Delete
      - [ ] Import

### Bid UI
- [x] Endpoint to send bids 
- [x] Endpoint in solawiApi
- [x] Serializers added to BE

- [ ] Page to send a bid
  - [x] Serializers added to FE
  - [x] Action to send bid + Test
  - [ ] verify that route has a crypto parameter (how?)                

### Excel import / Export 
- [ ] of Bidders
- [ ] of Results of an auction

### Prosumer Management
Needs to be planned soon!!! (2024.12.1)


### Mail Service (needed for Prosumer management, double opt in)

### Access different Apis (Future)

### Roles and Rights
1. Check access rights using JwtPrinciple, for example in an Action like ReceiveContextual or after it
2. Idea: one could pass the context in the header of the request, action to be performed and resources are given by endpoint and the input data 
    - Fundamental checks can be done like that
    - When it comes to more details checks on complex resource structures, one has to provide a specific action 
### Setup E2E Tests
- [ ] Choose Framework
- [ ] Make framework functional