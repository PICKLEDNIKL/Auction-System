# Auction System with Passive Replication

This project implements a distributed auctioning system designed to enhance dependability and fault tolerance using **passive replication**. It ensures consistent auction data across multiple server replicas and handles failures gracefully.

## Features
- **Passive Replication**: Three server replicas maintain a consistent view of auction data.
- **Fault Tolerance**: The system continues functioning even when replicas fail, supporting replica recovery and full turnover.
- **Core Auction Operations**: Includes operations such as bidding, listing items, closing auctions, and more.

## How to Run
1. **Start Replicas**:  
   Run each replica in a separate process using the command:  
   ```bash
   java Replica <ReplicaID>
   ```
2. **Start Frontend**:
   Run a frontend so that the client and server communicate indirectly:
   ```bash
   java FrontEnd
   ```
3. **Start Clients**:
   Run multiple clients that are allowed to auction:
   ```bash
   java Client
   ```

~ Year 3 Distributed Systems coursework
