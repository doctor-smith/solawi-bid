# CRUD Auctions

Creating and reading auctions is already possible.
So we concentrate on updates and deletion here.
 
Todos:
- [x] Add put, patch, delete functions to requests
- [x] Delete auctions:
    - [x] Add serializable data classes under ``` solawi-bid-api-data/solyton/solawi/bid(module|application)/.../data ```
    - [x] Add Endpoint to Api under ```solawi-bid-api-data/solyton/solawi/bid/api/solawi-api ```
    - [x] Add serializers for ApiTypes
        - [x] Frontend
        - [x] Backend
    - [x] If necessary, add transform functions to
        - Backend
            - [x] ```EntityType -> ApiType ```
            - [x] ```ApiType -> EntityType  ```
        - Frontend
            - [x] ```LensType -> ApiType```
            - [x] ```ApiType -> LensType```
    - [x] Add corresponding Action to frontend
    - [x] Implement endpoint
    - [x] Tests
        - [x] Frontend
        - [x] Backend

- [ ] Update auctions:
    - [x] Add serializable data classes under ``` solawi-bid-api-data/solyton/solawi/bid(module|application)/.../data ```
    - [x] Add Endpoint to Api under ```solawi-bid-api-data/solyton/solawi/bid/api/solawi-api ```
    - [x] Add serializers for ApiTypes
        - [x] Frontend
        - [x] Backend
    - [ ] If necessary, add transform functions to
        - Backend
            - [x] ```EntityType -> ApiType ```
            - [x] ```ApiType -> EntityType  ```
        - Frontend
            - [ ] ```LensType -> ApiType```
            - [ ] ```ApiType -> LensType```
    - [ ] Add corresponding Action to frontend
    - [x] Implement endpoint
    - [ ] Implement business logic
    - [ ] Tests
        - [ ] Frontend
        - [ ] Backend

- [ ] Add / Import Bidders
  - [x] Add serializable data classes under ``` solawi-bid-api-data/solyton/solawi/bid(module|application)/.../data ```
  - [x] Add Endpoint to Api under ```solawi-bid-api-data/solyton/solawi/bid/api/solawi-api ```
  - [x] Add serializers for ApiTypes
      - [x] Frontend
      - [x] Backend
  - [ ] If necessary, add transform functions to
      - Backend (Not necessary, done for all types)
          - [ ] ```EntityType -> ApiType ```
          - [ ] ```ApiType -> EntityType  ```
      - Frontend
          - [ ] ```LensType -> ApiType```
          - [ ] ```ApiType -> LensType```
  - [ ] Add corresponding Action to frontend
  - [x] Implement endpoint
  - [ ] Tests
      - [ ] Frontend
      - [ ] Backend
