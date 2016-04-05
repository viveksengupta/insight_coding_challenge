Project name: insight_coding_challenge
Vivek's solution to coding challenge for Insight data engineering fellowship



<<<<<<<<<<<<<<<<<<<< Dependencies <<<<<<<<<<<<<<<<<<<<

1. org.json.jar (JSON library for parsing JSON)
2. commons.collections-2.0.jar (Apache Commons Collections library for Binary Heap)

These jar files are in the src directory itself



<<<<<<<<<<<<<<<<<<<< Approach <<<<<<<<<<<<<<<<<<<<

Since I had to optimize for time and not space, I decided not to use a graph data structure at all.
The problem is very specific and I have devised a specific solution to it and not a generic one.
I have used 4 different data structures.

/* ========== 1. dateHeap ========== */
A min heap maintaining the timestamps of tweets which are in the 60 second scope.
When we process a new tweet, we keep on extracting the min from this heap till we find a tweet that didn't go out of scope.
We add the timestamp of the current tweet to the heap.

/* ========== 2. dateEdgeMap ========== */
A HashMap where dates(timestamps) are keys and a list of formatted edges is the value.
For an edge between vertices "abc" and "xyz", the formatted edge string would look like abc-xyz.
If multiple tweets come in at the same time, we will add the new edges being formed to the list

/* ========== 3. edgeContributionMap ========== */
A HashMap with edge strings as keys and the repetition factors as values.
If an edge comes from multiple tweets, then the contribution factor for that would be the same as the number of such tweets.
This way, we can identify whether an edge would still be there once one of such contributing tweets go out of scope.

/* ========== 4. vertexDegreeMap ========== */
A HashMap with vertices as keys and degrees of them as values.
Once the value of an edge goes to zero in the edgeContributionMap, we know that all the tweets for that edge are out of scope.
At this point, we can delete the edge.
Once we delete the edge, we can decrease the degree of the associated vertices.
If any of those degrees go to zero, we will delete the vertex from the map and decrease the total number of vertices.
Most of the stuff could be achieved through the other 3 data structures.
But without this, we can't keep track whether a vertex is to be deleted from the hashtag graph.



<<<<<<<<<<<<<<<<<<<< Performance <<<<<<<<<<<<<<<<<<<<

I had to optimize for time. So I used a combination of fast data structures.
The heap inserts and deletes run in O(log n)
The HashMap inserts and deletes are O(1 + alpha) where alpha is the load factor. This should be almost constant.
Moreover, in case of collisions, Java 8 uses a BST instead of a Linked List for chaining.
However, if the initial capacity is declared as per the usage expectations, we can achieve minimum collisions.

On the contrary, if I had used an adjacency list representation of a graph, search would take O(degree) of a vertex.
That would be really prohibitive for a fast response system. Hence, I avoided using graphs.



<<<<<<<<<<<<<<<<<<<< Concerns <<<<<<<<<<<<<<<<<<<<

I am an MS student taking coursework and also grading a course.
I had a lot at hand and couldn't spend as much time as I would have liked.
Hence, I could not test my code very rigorously.
I mostly performed ad hoc testing.
But I tried to cover all the corner cases.