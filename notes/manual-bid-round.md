# Manual

## Bid Round
1. Create an auction
2. Add values for benchmark, target amound, solidarity contribution, Default Bid to the auction record
3. Import bidders
    - via an api
    - via excel import
   
    Bidders carry the following information
    - username
    - number of shares
    - amount
4. Create a bid round
    
    As a result you get a crypto link 
5. Give the link to the bidders and ask them to bid on the page ```baseUrl/bid/send/crypto-part```
6. When the round is over, stop the round and 
   - download the result to be save
   - check the evaluation of the round
7. If necessary, create a new round and repeat the process