Replicated Concurrency Control and Recovery
(RepCRec for short)

implemented a distributed database, complete
with multiversion concurrency control, deadlock avoidance, replication, and
failure recovery. 

Data:
The data consists of 20 distinct variables x1, ..., x20 (the numbers between
1 and 20 will be referred to as indexes below). There are 10 sites
numbered 1 to 10. A copy is indicated by a dot. Thus, x6.2 is the copy of
variable x6 at site 2. The odd indexed variables are at one site each (i.e.
1 + index number mod 10 ). For example, x3 and x13 are both at site 4.
Even indexed variables are at all sites. Each variable xi is initialized to the
value 10i. Each site has an independent lock table. If that site fails, the
lock table is erased.

Algorithms used:
1. available copies approach to replication using two
phase locking (using read and write locks) at each site and validation at
commit time.
2. Avoid deadlocks using the wait-die protocol in which older transactions
wiat for younger ones, but younger ones abort rather than wait for older
ones. This implies that your system must keep track of the oldest transaction
time of any transaction holding a lock.
3. Read-only transactions should use multiversion read consistency.

Execution file format:

begin(T1)
begin(T2)
begin(T3)
W(T1, x1,5); W(T3, x2,32)
W(T2, x1,17); — will cause T2 to die because it cannot wait for an older
lock
end(T1); begin(T4)
W(T4, x4,35); W(T3, x5,21)
W(T4,x2,21); W(T3,x4,23) — T4 will die, freeing the lock on x4 allowing
T3 to finish

