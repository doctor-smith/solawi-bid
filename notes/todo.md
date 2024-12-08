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
      - [x] Create Auctions
      - [ ] Update Auctions
      - [ ] Delete Auctions
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
- [ ] Page to send a bid

### Excel import / Export 
- [ ] of Bidders
- [ ] of Results of an auction

### Prosumer Management
Needs to be planned soon!!! (2024.12.1)


### Mail Service (needed for Prosumer management, double opt in)

### Access different Apis (Future)

### Roles and Rights
TODO

### Setup E2E Tests
- [ ] Choose Framework
- [ ] Make framework functional