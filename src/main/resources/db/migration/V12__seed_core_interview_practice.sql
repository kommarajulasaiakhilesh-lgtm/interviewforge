-- Starter content for a new local installation. These are original InterviewForge examples,
-- not questions attributed to any employer or copied from an interview report.
INSERT INTO topics (id, name, slug, description, active, created_at, updated_at)
VALUES
    (md5('interviewforge-topic:dsa')::uuid, 'Data Structures and Algorithms', 'data-structures-algorithms', 'Core problem-solving patterns, complexity, and common data structures.', TRUE, now(), now()),
    (md5('interviewforge-topic:oop')::uuid, 'Object-Oriented Design', 'object-oriented-design', 'Encapsulation, composition, inheritance, and maintainable object design.', TRUE, now(), now()),
    (md5('interviewforge-topic:sql')::uuid, 'SQL and Databases', 'sql-databases', 'Queries, transactions, indexes, and relational data modeling.', TRUE, now(), now()),
    (md5('interviewforge-topic:os')::uuid, 'Operating Systems', 'operating-systems', 'Processes, threads, concurrency, and resource coordination.', TRUE, now(), now()),
    (md5('interviewforge-topic:networks')::uuid, 'Computer Networks', 'computer-networks', 'HTTP, DNS, transport protocols, and network fundamentals.', TRUE, now(), now()),
    (md5('interviewforge-topic:system-design')::uuid, 'System Design', 'system-design', 'Scalability, reliability, caching, and distributed-system trade-offs.', TRUE, now(), now()),
    (md5('interviewforge-topic:behavioral')::uuid, 'Behavioral Interviews', 'behavioral-interviews', 'Clear evidence-based stories about collaboration, learning, and ownership.', TRUE, now(), now())
ON CONFLICT DO NOTHING;

INSERT INTO companies (id, name, slug, description, active, created_at, updated_at)
VALUES (md5('interviewforge-company:general-role-library')::uuid, 'General Role Library', 'general-role-library',
        'Generic interview preparation tracks. This is a role library, not a hiring company.', TRUE, now(), now())
ON CONFLICT DO NOTHING;

INSERT INTO company_roles (id, company_id, name, slug, description, active, created_at, updated_at)
SELECT md5('interviewforge-role:' || role.slug)::uuid, company.id, role.name, role.slug, role.description, TRUE, now(), now()
FROM companies company
CROSS JOIN (VALUES
    ('backend-engineer', 'Backend Engineer', 'Practice server-side APIs, data stores, concurrency, and system design.'),
    ('data-analyst', 'Data Analyst', 'Practice SQL, data reasoning, and clear communication of findings.'),
    ('devops-engineer', 'DevOps Engineer', 'Practice networking, reliability, operating systems, and system design.'),
    ('frontend-engineer', 'Frontend Engineer', 'Practice object-oriented design, data structures, networking, and behavioral questions.'),
    ('full-stack-developer', 'Full Stack Developer', 'Practice programming fundamentals, databases, networking, and system design.'),
    ('java-developer', 'Java Developer', 'Practice object-oriented design, algorithms, databases, and concurrency.'),
    ('qa-engineer', 'QA Engineer', 'Practice problem-solving, systems fundamentals, and quality-focused communication.'),
    ('software-engineer', 'Software Engineer', 'Practice core technical concepts and structured behavioral answers.')
) AS role(slug, name, description)
WHERE company.slug = 'general-role-library'
ON CONFLICT DO NOTHING;

INSERT INTO skills (id, name, slug, description, active, created_at, updated_at)
VALUES
    (md5('interviewforge-skill:dsa')::uuid, 'Problem Solving and Algorithms', 'problem-solving-algorithms', 'Select and explain correct data structures and algorithms.', TRUE, now(), now()),
    (md5('interviewforge-skill:oop')::uuid, 'Object-Oriented Design', 'object-oriented-design', 'Design understandable, extensible object-oriented software.', TRUE, now(), now()),
    (md5('interviewforge-skill:sql')::uuid, 'Database Fundamentals', 'database-fundamentals', 'Query and reason about persistent relational data.', TRUE, now(), now()),
    (md5('interviewforge-skill:os')::uuid, 'Concurrency and Operating Systems', 'concurrency-operating-systems', 'Reason about processes, threads, and shared resources.', TRUE, now(), now()),
    (md5('interviewforge-skill:networks')::uuid, 'Networking Fundamentals', 'networking-fundamentals', 'Explain common network protocols and request flows.', TRUE, now(), now()),
    (md5('interviewforge-skill:system-design')::uuid, 'System Design', 'system-design', 'Make and explain practical scalability and reliability choices.', TRUE, now(), now()),
    (md5('interviewforge-skill:behavioral')::uuid, 'Communication and Collaboration', 'communication-collaboration', 'Give concise evidence-based examples of professional behavior.', TRUE, now(), now())
ON CONFLICT DO NOTHING;

INSERT INTO skill_topics (id, skill_id, topic_id, relevance)
SELECT md5('interviewforge-skill-topic:' || mapping.skill_slug)::uuid,
       md5('interviewforge-skill:' || mapping.skill_slug)::uuid, topic.id, 5
FROM (VALUES
    ('dsa', 'data-structures-algorithms'),
    ('oop', 'object-oriented-design'),
    ('sql', 'sql-databases'),
    ('os', 'operating-systems'),
    ('networks', 'computer-networks'),
    ('system-design', 'system-design'),
    ('behavioral', 'behavioral-interviews')
) AS mapping(skill_slug, topic_slug)
JOIN topics topic ON topic.slug = mapping.topic_slug
ON CONFLICT DO NOTHING;

INSERT INTO role_skills (id, role_id, skill_id, importance)
SELECT md5('interviewforge-role-skill:' || role.id::text || ':' || skill.id::text)::uuid,
       role.id, skill.id, 3
FROM company_roles role
JOIN companies company ON company.id = role.company_id AND company.slug = 'general-role-library'
CROSS JOIN skills skill
WHERE skill.slug IN ('problem-solving-algorithms', 'object-oriented-design', 'database-fundamentals',
                     'concurrency-operating-systems', 'networking-fundamentals', 'system-design', 'communication-collaboration')
ON CONFLICT DO NOTHING;

WITH question_seed(topic_slug, question_key, title, question_text, difficulty, options_json, correct_index,
                   correct_answer, explanation, option_explanations_json, theory_notes, workplace_example,
                   misconception_label, scenario_context, interview_stage) AS (VALUES
    ('data-structures-algorithms', 'binary-search', 'Binary search prerequisite', 'You need to find a value in a collection using binary search. What property must the collection have for the standard algorithm to be correct?', 'EASY',
     jsonb_build_array('The values are sorted by the searched key', 'The collection is stored in a linked list', 'Every value is unique', 'The collection has an even number of items'), 0,
     'The values are sorted by the searched key', 'Binary search discards half of the remaining search interval after each comparison. That decision is only valid when values are ordered.',
     jsonb_build_array('Correct. Ordering tells the algorithm which half can be discarded.', 'A linked list does not provide efficient random access and does not make binary search correct by itself.', 'Duplicates are allowed in sorted data; the algorithm can still find a matching value.', 'The collection size can be odd or even.'),
     'Binary search takes O(log n) comparisons on sorted data with efficient indexed access.', 'A service can use binary search over a sorted in-memory array of version numbers.', 'Assuming repeated values invalidate binary search', 'Choose the data structure property needed before selecting a search algorithm.', 'TECHNICAL_SCREEN'),
    ('data-structures-algorithms', 'cycle-detection', 'Detecting a linked-list cycle', 'Which approach detects a cycle in a singly linked list using O(1) extra space?', 'MEDIUM',
     jsonb_build_array('Use a slow pointer moving one node and a fast pointer moving two', 'Copy every node into a second linked list', 'Sort the node values and compare adjacent values', 'Count nodes until the list ends'), 0,
     'Use a slow pointer moving one node and a fast pointer moving two', 'If a cycle exists, the faster pointer eventually laps the slower pointer. The method uses constant extra space.',
     jsonb_build_array('Correct. Floyd’s cycle-detection method uses two pointers and constant space.', 'Copying nodes uses O(n) extra space and does not directly prove the original list is cyclic.', 'Sorting values is unrelated to link structure and changes the problem.', 'A cyclic list never reaches an end, so this loop would not terminate.'),
     'Floyd’s algorithm runs in O(n) time and O(1) extra space.', 'A linked-list based workflow engine can validate that a chain of transitions does not loop unexpectedly.', 'Confusing duplicate values with a cycle', 'Choose a cycle-detection strategy under a constant-memory constraint.', 'TECHNICAL_SCREEN'),
    ('data-structures-algorithms', 'hash-map-average', 'Hash-map lookup cost', 'With a well-distributed hash function and controlled load factor, what is the expected average lookup time in a hash map?', 'EASY',
     jsonb_build_array('O(1)', 'O(log n)', 'O(n log n)', 'O(n²)'), 0,
     'O(1)', 'A hash map computes a bucket location directly; expected lookup work stays constant when collisions are kept under control.',
     jsonb_build_array('Correct for expected average lookup under the stated assumptions; worst-case behavior can be O(n).', 'O(log n) is typical of a balanced search tree, not an average hash-map lookup.', 'This is not the usual expected lookup bound for a hash map.', 'This is not the expected lookup bound under a controlled load factor.'),
     'Average-case complexity depends on assumptions about hashing and collision handling; worst-case complexity can be linear.', 'A request handler can map a session token to its active session using a hash map.', 'Treating average complexity as a worst-case guarantee', 'State the expected performance and its assumptions.', 'TECHNICAL_SCREEN'),
    ('object-oriented-design', 'composition', 'Choosing composition', 'A reporting service must switch between several export formats at runtime. Which design usually keeps the service most flexible?', 'MEDIUM',
     jsonb_build_array('Inject an exporter interface and compose the service with the chosen exporter', 'Create a subclass of the service for every exporter and runtime combination', 'Put all format-specific branches in one growing conditional', 'Duplicate the reporting service for each output format'), 0,
     'Inject an exporter interface and compose the service with the chosen exporter', 'Composition lets the service delegate formatting to a replaceable collaborator without multiplying service subclasses.',
     jsonb_build_array('Correct. A stable abstraction and injected implementation separate reporting from formatting.', 'Subclass combinations grow quickly and couple behavior to inheritance structure.', 'A large conditional makes each new format require changes to the central service.', 'Duplication makes fixes and behavior drift across implementations more likely.'),
     'Favor composition when behavior varies independently and should be replaceable.', 'A reporting application can inject a CSV, JSON, or PDF exporter based on the requested output.', 'Using inheritance for every behavior combination', 'Select a design that permits format changes without rewriting the reporting workflow.', 'TECHNICAL_SCREEN'),
    ('object-oriented-design', 'liskov-substitution', 'Substitutable implementations', 'A function accepts a base type. What should be true of every subtype for callers to safely use it?', 'MEDIUM',
     jsonb_build_array('The subtype preserves the base type’s behavioral promises', 'The subtype must add at least one new public method', 'The subtype must use the same internal data structures', 'The subtype must be constructed only by the base type'), 0,
     'The subtype preserves the base type’s behavioral promises', 'Substitutability is about observable behavior and valid expectations, not matching implementation details.',
     jsonb_build_array('Correct. Callers should be able to rely on the base contract when given a subtype.', 'A subtype can add methods, but that is not required for safe substitution.', 'Internal representation can differ while preserving the same contract.', 'Construction mechanisms do not define substitutability.'),
     'The Liskov Substitution Principle says subtypes should be usable wherever their base type is expected without breaking correctness.', 'A storage adapter should honor the same save and retrieval guarantees whether backed by memory or a database.', 'Confusing shared implementation with shared contract', 'Evaluate subtype safety from a caller’s perspective.', 'TECHNICAL_SCREEN'),
    ('object-oriented-design', 'interface-segregation', 'Small client-focused interfaces', 'Several clients use different subsets of a large interface. What is the best first refactoring direction?', 'MEDIUM',
     jsonb_build_array('Split the interface into focused contracts so each client depends only on what it uses', 'Make every method a default method', 'Copy the full interface into each client', 'Make the interface implementation global and static'), 0,
     'Split the interface into focused contracts so each client depends only on what it uses', 'Focused interfaces reduce unnecessary coupling and keep implementations from depending on unrelated operations.',
     jsonb_build_array('Correct. Clients should not be forced to depend on methods they do not use.', 'Default methods do not remove the coupling to the broad contract.', 'Copying definitions creates drift and does not solve the oversized abstraction.', 'Global static access introduces different coupling and lifecycle problems.'),
     'The Interface Segregation Principle favors small interfaces tailored to client needs.', 'A read-only dashboard should depend on a query interface, not a broad repository interface that also exposes destructive operations.', 'Treating one large interface as simpler for every client', 'Reduce dependencies while preserving the behavior each client actually needs.', 'TECHNICAL_SCREEN'),
    ('sql-databases', 'group-having', 'Finding duplicate values', 'You need to list email addresses that appear more than once in a users table. Which query pattern is appropriate?', 'EASY',
     jsonb_build_array('GROUP BY email HAVING COUNT(*) > 1', 'WHERE COUNT(*) > 1 GROUP BY email', 'ORDER BY email LIMIT 1', 'DISTINCT email WHERE email IS NULL'), 0,
     'GROUP BY email HAVING COUNT(*) > 1', 'GROUP BY forms one group per email, and HAVING filters groups using an aggregate condition.',
     jsonb_build_array('Correct. HAVING filters grouped results after the aggregate is computed.', 'WHERE filters rows before grouping and cannot directly filter on COUNT(*).', 'Ordering and limiting do not identify duplicate groups.', 'DISTINCT removes duplicates rather than identifying them.'),
     'Use WHERE for row predicates before aggregation and HAVING for aggregate predicates after grouping.', 'A data-quality job can report duplicate account emails before a uniqueness constraint is added.', 'Using WHERE for aggregate filters', 'Select the SQL clause that filters aggregate groups.', 'TECHNICAL_SCREEN'),
    ('sql-databases', 'atomicity', 'Transaction atomicity', 'A bank transfer debits one account and credits another. Which transaction property prevents only one of those writes from being committed?', 'EASY',
     jsonb_build_array('Atomicity', 'Availability', 'Normalization', 'Sharding'), 0,
     'Atomicity', 'Atomicity makes the set of transaction operations succeed together or roll back together.',
     jsonb_build_array('Correct. Atomicity prevents a partial transfer from being committed.', 'Availability concerns whether a system can serve requests.', 'Normalization is a data-modeling practice.', 'Sharding distributes data across partitions or machines.'),
     'ACID atomicity is the all-or-nothing property of a transaction.', 'If the credit operation fails, the debit is rolled back before the transfer is reported as failed.', 'Assuming sequential statements are automatically all-or-nothing', 'Identify the transaction guarantee needed to prevent a partial update.', 'TECHNICAL_SCREEN'),
    ('sql-databases', 'index-tradeoff', 'Index trade-offs', 'A frequently queried column has no index. What is the main trade-off when adding a suitable index?', 'MEDIUM',
     jsonb_build_array('Reads may become faster, while writes and storage use can increase', 'Reads and writes always become faster with no storage cost', 'The table no longer needs transaction logging', 'The index guarantees every query uses it'), 0,
     'Reads may become faster, while writes and storage use can increase', 'An index can avoid scanning every row for suitable predicates, but must be maintained as data changes and consumes space.',
     jsonb_build_array('Correct. Indexes exchange extra storage and write maintenance for faster access on matching query patterns.', 'Indexes have storage and maintenance costs and do not accelerate every operation.', 'Indexes do not replace transaction logging.', 'The query planner may choose another plan when it estimates that is cheaper.'),
     'Indexes are access structures whose value depends on workload, selectivity, and maintenance cost.', 'Before adding an index to a high-write event table, compare the query benefit against insert overhead and disk usage.', 'Adding an index without considering write workload', 'Reason about both read benefit and operational cost.', 'TECHNICAL_SCREEN'),
    ('operating-systems', 'thread-memory', 'What threads share', 'Within a single process, which resource is generally shared by its threads?', 'EASY',
     jsonb_build_array('The process address space and heap', 'Each thread’s stack', 'Each thread’s program counter', 'Each thread’s register values'), 0,
     'The process address space and heap', 'Threads in a process share the process memory and resources, while each thread has its own execution state and stack.',
     jsonb_build_array('Correct. Threads share the process address space, including its heap.', 'Each thread generally has its own stack.', 'Each thread has its own program counter.', 'Registers represent per-thread execution state.'),
     'Threads share process resources but maintain separate stacks and execution state.', 'Two request-handling threads can access a shared in-memory cache, so updates may need synchronization.', 'Assuming threads have fully isolated memory like processes', 'Distinguish shared process state from per-thread execution state.', 'TECHNICAL_SCREEN'),
    ('operating-systems', 'deadlock-conditions', 'Deadlock conditions', 'Which set contains the four classic necessary conditions for a deadlock?', 'HARD',
     jsonb_build_array('Mutual exclusion, hold and wait, no preemption, and circular wait', 'Caching, paging, scheduling, and context switching', 'Atomicity, consistency, isolation, and durability', 'Starvation, fairness, throughput, and latency'), 0,
     'Mutual exclusion, hold and wait, no preemption, and circular wait', 'A deadlock requires all four Coffman conditions to hold simultaneously.',
     jsonb_build_array('Correct. These are the four classic Coffman conditions.', 'These are operating-system concepts but are not the deadlock condition set.', 'These are ACID transaction properties.', 'These are performance and scheduling concerns, not the deadlock conditions.'),
     'Deadlock prevention can break at least one necessary condition, such as by enforcing a global lock order.', 'A service can reduce circular wait by requiring components to acquire locks in a consistent order.', 'Confusing general concurrency terms with necessary deadlock conditions', 'Recall the complete condition set before proposing a prevention strategy.', 'TECHNICAL_SCREEN'),
    ('operating-systems', 'race-condition', 'Race conditions', 'Two threads update the same counter without synchronization. The final value is sometimes lower than expected. What is the most likely cause?', 'MEDIUM',
     jsonb_build_array('A race condition in a non-atomic read-modify-write sequence', 'A DNS cache miss', 'A memory leak', 'A deadlock that always completes'), 0,
     'A race condition in a non-atomic read-modify-write sequence', 'The increments can interleave so both threads read the same old value and one update overwrites the other.',
     jsonb_build_array('Correct. Unsynchronized shared updates can lose increments through interleaving.', 'DNS resolution does not explain a shared counter update.', 'A memory leak concerns unreleased memory, not lost updates.', 'A deadlock does not complete; it blocks progress.'),
     'Protect shared mutable state with appropriate synchronization or use an atomic operation.', 'A concurrent metrics collector should use an atomic counter or a lock when increments can overlap.', 'Assuming a single increment statement is automatically atomic in application code', 'Explain why interleaving changes the final value.', 'TECHNICAL_SCREEN'),
    ('computer-networks', 'tcp-guarantees', 'TCP guarantees', 'Which property is provided by TCP but not generally guaranteed by UDP?', 'EASY',
     jsonb_build_array('Reliable, ordered delivery of a byte stream', 'Encryption of application data by default', 'A fixed maximum latency', 'Automatic understanding of application messages'), 0,
     'Reliable, ordered delivery of a byte stream', 'TCP retransmits lost data and presents an ordered byte stream; applications still need to define message boundaries.',
     jsonb_build_array('Correct. TCP provides reliable, ordered byte-stream delivery.', 'Encryption is normally provided by protocols such as TLS, not TCP itself.', 'TCP cannot guarantee a fixed latency.', 'TCP transports bytes and does not infer application message boundaries.'),
     'Transport protocols make different reliability and ordering trade-offs; neither implies application-level encryption.', 'A file upload generally benefits from TCP’s ordered reliable stream, while a real-time update may choose UDP and handle loss differently.', 'Assuming TCP provides encryption and message framing', 'Compare the transport-layer guarantee without attributing application-layer features.', 'TECHNICAL_SCREEN'),
    ('computer-networks', 'dns-purpose', 'DNS purpose', 'A browser is given a domain name such as api.example.com. What is DNS primarily used for?', 'EASY',
     jsonb_build_array('Resolving the domain name to network records such as IP addresses', 'Encrypting the HTTP request body', 'Choosing which database row to read', 'Guaranteeing that the server is available'), 0,
     'Resolving the domain name to network records such as IP addresses', 'DNS lets clients discover address and other records associated with a domain name.',
     jsonb_build_array('Correct. DNS resolves names to records used to locate services.', 'TLS encrypts transport data; DNS itself does not encrypt an HTTP body.', 'Database query execution is unrelated to DNS.', 'DNS resolution does not guarantee the destination service is healthy.'),
     'DNS is a distributed naming system. Resolution and service health are separate steps.', 'If DNS resolves correctly but the service is down, the browser still cannot complete the request.', 'Treating successful DNS resolution as proof the application is healthy', 'Identify where name resolution fits in a request path.', 'TECHNICAL_SCREEN'),
    ('computer-networks', 'put-idempotent', 'HTTP idempotency', 'Which HTTP method is designed to be idempotent when replacing a resource at a known URI?', 'MEDIUM',
     jsonb_build_array('PUT', 'POST', 'CONNECT', 'TRACE'), 0,
     'PUT', 'Repeating the same PUT request should leave the resource in the same intended state as applying it once.',
     jsonb_build_array('Correct. PUT is defined as idempotent for the target resource state.', 'POST is not generally idempotent; repeated requests may create multiple resources or actions.', 'CONNECT establishes a tunnel and is not the resource replacement method.', 'TRACE is a diagnostic method, not a resource replacement method.'),
     'Idempotency describes the effect of repeating a request, not whether the response body is identical.', 'A client can retry a timed-out PUT to set a profile field to the same value without creating another profile.', 'Equating idempotent with “the server is called only once”', 'Choose a method whose repeated application has the same intended effect.', 'TECHNICAL_SCREEN'),
    ('system-design', 'horizontal-scaling', 'Horizontal scaling', 'A stateless API needs to handle more concurrent traffic. Which change most directly adds horizontal capacity?', 'MEDIUM',
     jsonb_build_array('Run additional API instances behind a load balancer', 'Increase only the log retention period', 'Move all requests to one larger thread', 'Disable health checks'), 0,
     'Run additional API instances behind a load balancer', 'Horizontal scaling adds instances and distributes requests; statelessness makes instances easier to add independently.',
     jsonb_build_array('Correct. Additional instances can serve traffic in parallel when shared state is handled appropriately.', 'Log retention does not directly add request-serving capacity.', 'One larger thread does not provide a general horizontal scaling strategy.', 'Disabling health checks can route traffic to unhealthy instances.'),
     'Horizontal scaling adds machines or instances; it also requires attention to shared state, dependencies, and bottlenecks.', 'An API tier can scale out while sessions and durable state remain in shared services.', 'Assuming a load balancer alone fixes every bottleneck', 'Choose the change that increases serving capacity while preserving health routing.', 'SYSTEM_DESIGN'),
    ('system-design', 'cache-tradeoff', 'Cache trade-offs', 'A read-heavy endpoint repeatedly loads slowly changing reference data from a database. What is a likely benefit and cost of adding a cache?', 'MEDIUM',
     jsonb_build_array('Lower repeated-read latency, with added invalidation and staleness concerns', 'Perfectly current data with no extra operational complexity', 'Elimination of the need for a database', 'Guaranteed lower latency for every request'), 0,
     'Lower repeated-read latency, with added invalidation and staleness concerns', 'A cache can reduce repeated database work, but the design must define expiry, invalidation, and acceptable staleness.',
     jsonb_build_array('Correct. Caching trades some freshness and operational simplicity for reduced repeated-read cost.', 'Caches can serve stale data and require operational decisions.', 'The database remains the durable source of truth in this design.', 'Cache misses, network hops, and invalidation can make some requests no faster or slower.'),
     'Cache only when the workload and freshness requirements justify the complexity.', 'A product catalog may tolerate a short cache lifetime, while account balances may require stricter consistency.', 'Assuming cached data is always fresh', 'Balance read performance against data freshness requirements.', 'SYSTEM_DESIGN'),
    ('system-design', 'async-queue', 'Asynchronous queues', 'An order service must accept bursts of work while a slower email service processes notifications. What can a durable queue provide?', 'MEDIUM',
     jsonb_build_array('Buffer work and decouple the order request from notification processing', 'Make email delivery instantaneous', 'Remove the need to handle duplicate messages', 'Guarantee that every downstream service is always available'), 0,
     'Buffer work and decouple the order request from notification processing', 'A queue can absorb bursts and let a consumer process work independently, while the system still needs retry and idempotency behavior.',
     jsonb_build_array('Correct. Queueing decouples producer and consumer timing and can smooth bursts.', 'A queue does not make a slow external email provider faster.', 'Messages may be delivered more than once; consumers should handle retries safely.', 'Queues improve decoupling but cannot make downstream dependencies permanently available.'),
     'Asynchronous messaging adds resilience and buffering but requires delivery, retry, ordering, and idempotency decisions.', 'An order can be committed first, then a worker sends confirmation email with retries.', 'Treating queues as a guarantee of exactly-once downstream effects', 'Select the benefit that queueing provides without promising more than it can.', 'SYSTEM_DESIGN'),
    ('behavioral-interviews', 'star-method', 'Structuring a behavioral answer', 'Which structure helps a behavioral answer explain context, your responsibility, your actions, and the result?', 'EASY',
     jsonb_build_array('Situation, Task, Action, Result', 'Summary, Theory, Assumption, Response', 'Scope, Timeline, Architecture, Review', 'Strength, Talent, Ambition, Rating'), 0,
     'Situation, Task, Action, Result', 'STAR keeps an example concrete: establish context and responsibility, explain your actions, and state the result or learning.',
     jsonb_build_array('Correct. STAR is a common structure for concise evidence-based examples.', 'This is not the STAR framework.', 'These are project-planning concepts, not the behavioral answer structure.', 'These labels do not ensure a concrete example or outcome.'),
     'Keep the situation and task brief, give most detail to your own actions, then quantify the result when possible.', 'For a conflict question, describe the team context, your responsibility, what you did, and what changed.', 'Giving background without explaining personal actions', 'Choose a structure that makes a past example easy to follow.', 'BEHAVIORAL'),
    ('behavioral-interviews', 'conflict-response', 'Responding to disagreement', 'A teammate disagrees with your technical proposal. What is the strongest first response?', 'MEDIUM',
     jsonb_build_array('Ask about their concerns, compare evidence against shared goals, and agree on a way to decide', 'Repeat your proposal more forcefully until the discussion ends', 'Escalate immediately without discussing the disagreement', 'Avoid the teammate and implement your preferred option'), 0,
     'Ask about their concerns, compare evidence against shared goals, and agree on a way to decide', 'This response shows curiosity, evidence-based reasoning, and collaboration while keeping the discussion focused on the outcome.',
     jsonb_build_array('Correct. Understand the concern, use evidence, and agree on decision criteria.', 'Forcefulness does not resolve the underlying trade-off and can damage trust.', 'Escalation may be appropriate later, but first try a direct constructive discussion when safe.', 'Avoidance prevents alignment and may create unreviewed risk.'),
     'Strong conflict examples show listening, shared goals, evidence, and a constructive resolution.', 'A team can compare options using latency, complexity, and delivery risk before selecting an approach.', 'Treating disagreement as a contest to win', 'Choose an action that makes progress while respecting the other person’s perspective.', 'BEHAVIORAL'),
    ('behavioral-interviews', 'failure-learning', 'Explaining a setback', 'When asked about a professional mistake, which answer is most useful to an interviewer?', 'MEDIUM',
     jsonb_build_array('A specific example, your responsibility, the impact, what you changed, and evidence the lesson stuck', 'A story where another person caused every problem', 'A claim that you have never made a mistake', 'A vague lesson with no example or follow-up action'), 0,
     'A specific example, your responsibility, the impact, what you changed, and evidence the lesson stuck', 'A credible answer demonstrates reflection and changed behavior rather than blame or a polished claim without evidence.',
     jsonb_build_array('Correct. Ownership plus a concrete corrective action makes the learning credible.', 'Blaming others avoids showing your own judgment and response.', 'An absolute claim is difficult to trust and provides no evidence of learning.', 'A lesson without an example or action is hard to evaluate.'),
     'Choose a bounded example and focus on the decision you would handle differently now.', 'After a missed handoff, you might introduce a checklist and show that later releases had fewer omissions.', 'Confusing accountability with self-criticism or blame', 'Show how reflection changed later behavior.', 'BEHAVIORAL')
)
INSERT INTO questions (id, topic_id, title, question_text, difficulty, type, options_json, correct_option_index,
                       answer_text, explanation, option_explanations_json, theory_notes, workplace_example,
                       misconception_label, scenario_context, interview_stage, source_type, source_label,
                       published, archived, created_at, updated_at)
SELECT md5('interviewforge-question:' || q.topic_slug || ':' || q.question_key)::uuid,
       topic.id, q.title, q.question_text, q.difficulty, 'MCQ', q.options_json, q.correct_index,
       q.correct_answer, q.explanation, q.option_explanations_json, q.theory_notes, q.workplace_example,
       q.misconception_label, q.scenario_context, q.interview_stage, 'ORIGINAL', 'InterviewForge original',
       TRUE, FALSE, now(), now()
FROM question_seed q
JOIN topics topic ON topic.slug = q.topic_slug
ON CONFLICT DO NOTHING;
