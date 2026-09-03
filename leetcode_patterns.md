# 🚀 The Ultimate LeetCode Problem-Solving Patterns & Algorithmic Paradigm Master Guide

> **A Comprehensive, Production-Grade Curriculum in Java: From Naive Brute Force to Dynamic Programming, Visual Execution Traces, Multi-Approach Solutions, and 18 Master Problem-Solving Patterns.**

---

## 📑 Table of Contents

- [🚀 The Ultimate LeetCode Problem-Solving Patterns \& Algorithmic Paradigm Master Guide](#-the-ultimate-leetcode-problem-solving-patterns--algorithmic-paradigm-master-guide)
  - [📑 Table of Contents](#-table-of-contents)
  - [🧠 Phase 1: The Algorithmic Paradigm Shift — Brute Force vs. Recursion vs. Dynamic Programming](#-phase-1-the-algorithmic-paradigm-shift--brute-force-vs-recursion-vs-dynamic-programming)
    - [1.1 What is Brute Force? (The Exhaustive Search Space)](#11-what-is-brute-force-the-exhaustive-search-space)
    - [1.2 What is Recursion \& Divide-and-Conquer? (The Call Stack Engine)](#12-what-is-recursion--divide-and-conquer-the-call-stack-engine)
    - [1.3 What is Dynamic Programming (DP)? (Optimal Substructure \& Overlapping Subproblems)](#13-what-is-dynamic-programming-dp-optimal-substructure--overlapping-subproblems)
    - [1.4 The 4-Stage DP Evolutionary Pipeline](#14-the-4-stage-dp-evolutionary-pipeline)
    - [1.5 Master Evolutionary Case Studies (Side-by-Side Java Implementations)](#15-master-evolutionary-case-studies-side-by-side-java-implementations)
      - [Case Study 1: Fibonacci Numbers / Climbing Stairs](#case-study-1-fibonacci-numbers--climbing-stairs)
      - [Case Study 2: The 0/1 Knapsack Problem](#case-study-2-the-01-knapsack-problem)
      - [Case Study 3: Longest Common Subsequence (LCS)](#case-study-3-longest-common-subsequence-lcs)
      - [Case Study 4: Coin Change (Minimum Coins to Make Amount)](#case-study-4-coin-change-minimum-coins-to-make-amount)
  - [🧩 Phase 2: The 18 Essential LeetCode Problem-Solving Patterns](#-phase-2-the-18-essential-leetcode-problem-solving-patterns)
    - [Pattern 1: Two Pointers Pattern](#pattern-1-two-pointers-pattern)
    - [Pattern 2: Sliding Window Pattern](#pattern-2-sliding-window-pattern)
    - [Pattern 3: Fast \& Slow Pointers (Floyd's Tortoise and Hare)](#pattern-3-fast--slow-pointers-floyds-tortoise-and-hare)
    - [Pattern 4: Merge Intervals Pattern](#pattern-4-merge-intervals-pattern)
    - [Pattern 5: Cyclic Sort Pattern](#pattern-5-cyclic-sort-pattern)
    - [Pattern 6: In-place Reversal of a Linked List Pattern](#pattern-6-in-place-reversal-of-a-linked-list-pattern)
    - [Pattern 7: Tree Breadth-First Search (BFS / Level Order)](#pattern-7-tree-breadth-first-search-bfs--level-order)
    - [Pattern 8: Tree Depth-First Search (DFS / Backtracking on Trees)](#pattern-8-tree-depth-first-search-dfs--backtracking-on-trees)
    - [Pattern 9: Two Heaps / Dynamic Median Tracking](#pattern-9-two-heaps--dynamic-median-tracking)
    - [Pattern 10: Subsets, Permutations \& Combinations (Backtracking)](#pattern-10-subsets-permutations--combinations-backtracking)
    - [Pattern 11: Modified Binary Search](#pattern-11-modified-binary-search)
    - [Pattern 12: Top 'K' Elements (Heap \& Quickselect)](#pattern-12-top-k-elements-heap--quickselect)
    - [Pattern 13: Dynamic Programming — Knapsack Variants (0/1 \& Unbounded)](#pattern-13-dynamic-programming--knapsack-variants-01--unbounded)
    - [Pattern 14: Dynamic Programming — Longest Common Subsequence (LCS) \& String DP](#pattern-14-dynamic-programming--longest-common-subsequence-lcs--string-dp)
    - [Pattern 15: Dynamic Programming — Longest Increasing Subsequence (LIS) \& 1D DP](#pattern-15-dynamic-programming--longest-increasing-subsequence-lis--1d-dp)
    - [Pattern 16: Monotonic Stack \& Monotonic Queue](#pattern-16-monotonic-stack--monotonic-queue)
    - [Pattern 17: Graph Algorithms (BFS, DFS, Topological Sort, Disjoint Set Union)](#pattern-17-graph-algorithms-bfs-dfs-topological-sort-disjoint-set-union)
    - [Pattern 18: Trie (Prefix Tree) \& Bit Manipulation](#pattern-18-trie-prefix-tree--bit-manipulation)

---

## 🧠 Phase 1: The Algorithmic Paradigm Shift — Brute Force vs. Recursion vs. Dynamic Programming

```
+---------------------------------------------------------------------------------------------------+
|                                 THE COMPUTATIONAL COMPLEXITY PYRAMID                              |
|                                                                                                   |
|     [ Stage 1: Brute Force / Naive Recursion ]  --> O(2^N) / O(N!) Time | Explores Every Branch   |
|                          |                                                                        |
|                          v  (Add Top-Down Memoization Cache)                                      |
|     [ Stage 2: Top-Down DP (Memoization) ]      --> O(N * W) Time       | Caches Subproblems      |
|                          |                                                                        |
|                          v  (Convert to Iterative Tabulation)                                     |
|     [ Stage 3: Bottom-Up DP (Tabulation) ]       --> O(N * W) Time       | Eliminates Call Stack   |
|                          |                                                                        |
|                          v  (State Compression / Rolling Array)                                   |
|     [ Stage 4: Space-Optimized DP ]             --> O(1) / O(W) Space   | Maximum Production Peak |
+---------------------------------------------------------------------------------------------------+
```

---

### 1.1 What is Brute Force? (The Exhaustive Search Space)

* **Plain English Definition**: Brute Force is the simplest problem-solving strategy: **try every conceivable possibility until you stumble upon the correct answer**.
* **How It Operates**:
  - For an array of size $N$, finding all pairs takes $O(N^2)$ iterations (nested loops).
  - Generating all subsets of size $N$ takes $O(2^N)$ possibilities (each element is either included or excluded).
  - Generating all permutations of size $N$ takes $O(N!)$ sequences.
* **Why Brute Force Fails on LeetCode (The TLE Problem)**:
  - Modern CPU clock frequencies execute roughly $10^8$ basic instructions per second ($100\text{ Million ops/sec}$).
  - A test case with $N = 40$ on an $O(2^N)$ algorithm requires $2^{40} \approx 1,099,511,627,776$ operations ($>10^{12}$).
  - Time required: $\frac{10^{12}}{10^8} \approx 10,000\text{ seconds}$ ($\approx 2.7\text{ hours}$).
  - LeetCode's evaluation sandbox terminates execution after **$2.0\text{ seconds}$**, issuing a **Time Limit Exceeded (TLE)** error.

---

### 1.2 What is Recursion & Divide-and-Conquer? (The Call Stack Engine)

* **Plain English Definition**: Recursion solves a large, intimidating problem by breaking it down into smaller, identical subproblems, delegating them to self-invoking function calls until reaching a trivial **Base Case**.
* **Under-the-Hood JVM Memory Dynamics**:
  - Every recursive invocation allocates a new **Stack Frame** on the OS Thread Stack memory.
  - A stack frame stores method arguments, local variables, and the return address.
  - If recursion depth exceeds $\approx 10,000$ calls without hitting a base case, the JVM exhausts stack memory and throws `java.lang.StackOverflowError`.

```
========================= RECURSION CALL STACK TRACE =========================
| Call: fib(4)                                                               |
|   -> pushes Frame [n=4, returnAddress=Line 12]                             |
|   -> calls fib(3)                                                          |
|        -> pushes Frame [n=3, returnAddress=Line 12]                        |
|        -> calls fib(2)                                                     |
|             -> pushes Frame [n=2, returnAddress=Line 12]                   |
|             -> calls fib(1) -> hits Base Case! returns 1 (pops Frame n=1)  |
|             -> calls fib(0) -> hits Base Case! returns 0 (pops Frame n=0)  |
|             -> returns 1 + 0 = 1 (pops Frame n=2)                          |
==============================================================================
```

---

### 1.3 What is Dynamic Programming (DP)? (Optimal Substructure & Overlapping Subproblems)

Dynamic Programming is **NOT** a complicated math trick; it is simply **intelligent recursion that remembers past answers to avoid doing redundant work**.

To apply Dynamic Programming, a problem MUST satisfy two mathematical prerequisites:

1. **Optimal Substructure**:
   * The optimal solution to the global problem can be constructed directly from the optimal solutions of its smaller subproblems.
   * *Example*: The shortest driving path from New York to Los Angeles passing through Chicago consists of `ShortestPath(NY -> Chicago) + ShortestPath(Chicago -> LA)`.
2. **Overlapping Subproblems**:
   * A naive recursive breakdown visits the **exact same subproblems over and over again** across divergent branches of its decision tree.
   * *Example*: In `fib(5)`, `fib(3)` is computed independently 2 times, `fib(2)` is computed 3 times, and `fib(1)` is computed 5 times!

```
                         fib(5)
                       /        \
                 fib(4)          fib(3)  <-- Duplicate Subproblem!
                /      \         /     \
           fib(3)     fib(2)   fib(2)  fib(1) <-- Duplicate Subproblem!
          /     \     /    \
      fib(2)  fib(1) fib(1) fib(0)
```

---

### 1.4 The 4-Stage DP Evolutionary Pipeline

| Stage | Name | Direction | Memory Structure | Time Complexity | Space Complexity |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Stage 1** | **Naive Brute Force** | Top-Down | Call Stack ($O(N)$ depth) | Exponential $O(2^N)$ | $O(N)$ Stack Memory |
| **Stage 2** | **Memoization** | Top-Down | Lookup Table (Heap) + Stack | Polynomial $O(N \cdot W)$ | $O(N \cdot W)$ Table + $O(N)$ Stack |
| **Stage 3** | **Tabulation** | Bottom-Up | 2D Array Grid (`dp[][]`) | Polynomial $O(N \cdot W)$ | $O(N \cdot W)$ Table, $O(1)$ Stack |
| **Stage 4** | **Space Optimization**| Bottom-Up | 1D Rolling Array (`dp[]`) | Polynomial $O(N \cdot W)$ | $O(W)$ or $O(1)$ Ultra-Lean Memory |

---

### 1.5 Master Evolutionary Case Studies (Side-by-Side Java Implementations)

#### Case Study 1: Fibonacci Numbers / Climbing Stairs

* **Problem Statement**: You are climbing a staircase. It takes $N$ steps to reach the top. Each time you can either climb $1$ or $2$ steps. In how many distinct ways can you climb to the top?

```java
package com.leetcode.dp.evolution;

import java.util.Arrays;

public class FibonacciEvolution {

    // =========================================================================
    // STAGE 1: Naive Recursive (Brute Force)
    // Time Complexity:  O(2^N) - Exponential explosion
    // Space Complexity: O(N)   - Maximum recursive call stack depth
    // Bottleneck: Recomputes identical subproblems thousands of times.
    // =========================================================================
    public int climbStairs_Stage1_BruteForce(int n) {
        if (n <= 1) return 1;
        return climbStairs_Stage1_BruteForce(n - 1) + climbStairs_Stage1_BruteForce(n - 2);
    }

    // =========================================================================
    // STAGE 2: Top-Down DP (Memoization)
    // Time Complexity:  O(N) - Each state from 0 to N is solved exactly ONCE
    // Space Complexity: O(N) Memo Array + O(N) Call Stack
    // Breakthrough: Returns cached results from memo[] in O(1) time.
    // =========================================================================
    public int climbStairs_Stage2_Memoization(int n) {
        int[] memo = new int[n + 1];
        Arrays.fill(memo, -1);
        return solveMemo(n, memo);
    }

    private int solveMemo(int n, int[] memo) {
        if (n <= 1) return 1;
        if (memo[n] != -1) return memo[n]; // Cache Hit (O(1) fast return)
        return memo[n] = solveMemo(n - 1, memo) + solveMemo(n - 2, memo);
    }

    // =========================================================================
    // STAGE 3: Bottom-Up DP (Tabulation)
    // Time Complexity:  O(N) - Single linear iterative loop
    // Space Complexity: O(N) - Table storage, ZERO recursive call stack
    // Breakthrough: Completely eliminates recursion and StackOverflowError risks.
    // =========================================================================
    public int climbStairs_Stage3_Tabulation(int n) {
        if (n <= 1) return 1;
        int[] dp = new int[n + 1];
        dp[0] = 1;
        dp[1] = 1;
        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[n];
    }

    // =========================================================================
    // STAGE 4: Space-Optimized DP (Rolling Variables)
    // Time Complexity:  O(N)
    // Space Complexity: O(1) - Constant auxiliary memory
    // Breakthrough: Notice dp[i] only ever needs the past TWO states (prev1, prev2).
    // =========================================================================
    public int climbStairs_Stage4_SpaceOptimized(int n) {
        if (n <= 1) return 1;
        int prev2 = 1; // Base state for dp[0]
        int prev1 = 1; // Base state for dp[1]
        for (int i = 2; i <= n; i++) {
            int current = prev1 + prev2;
            prev2 = prev1;
            prev1 = current;
        }
        return prev1;
    }
}
```

---

#### Case Study 2: The 0/1 Knapsack Problem

* **Problem Statement**: Given $N$ items, each with a `weight[i]` and `value[i]`, and a knapsack with maximum weight capacity $W$. Determine the maximum value you can carry without exceeding capacity $W$. Each item can either be taken once ($1$) or left behind ($0$).

```
VISUAL 2D TABULATION GRID (Items vs Weight Capacity):
Item (w=2, v=3), Item (w=3, v=4), Item (w=4, v=5), Capacity W = 5

      W=0   W=1   W=2   W=3   W=4   W=5
i=0 [  0     0     0     0     0     0  ] (No items)
i=1 [  0     0     3     3     3     3  ] (Item 1: w=2, v=3)
i=2 [  0     0     3     4     4     7  ] (Item 2: w=3, v=4) -> dp[2][5] = max(dp[1][5], v2 + dp[1][5-3]) = max(3, 4+3) = 7
i=3 [  0     0     3     4     5     7  ] (Item 3: w=4, v=5)
```

```java
package com.leetcode.dp.evolution;

import java.util.Arrays;

public class Knapsack01Evolution {

    // =========================================================================
    // STAGE 1: Naive Recursive (Brute Force)
    // Time Complexity:  O(2^N) - Every item creates 2 decision branches (Include / Exclude)
    // Space Complexity: O(N)   - Recursive call stack depth
    // =========================================================================
    public int knapsack_Stage1_BruteForce(int[] weights, int[] values, int W, int n) {
        // Base Case: No items left or capacity exhausted
        if (n == 0 || W == 0) return 0;

        // If current item exceeds remaining capacity, we MUST exclude it
        if (weights[n - 1] > W) {
            return knapsack_Stage1_BruteForce(weights, values, W, n - 1);
        }

        // Branch 1: Include current item (gain value, deduct weight)
        int include = values[n - 1] + knapsack_Stage1_BruteForce(weights, values, W - weights[n - 1], n - 1);
        // Branch 2: Exclude current item (capacity unchanged)
        int exclude = knapsack_Stage1_BruteForce(weights, values, W, n - 1);

        return Math.max(include, exclude);
    }

    // =========================================================================
    // STAGE 2: Top-Down DP (Memoization)
    // Time Complexity:  O(N * W) - Distinct subproblem states
    // Space Complexity: O(N * W) Heap Memory + O(N) Call Stack
    // =========================================================================
    public int knapsack_Stage2_Memoization(int[] weights, int[] values, int W) {
        int n = weights.length;
        int[][] memo = new int[n + 1][W + 1];
        for (int[] row : memo) Arrays.fill(row, -1);
        return solveKnapsackMemo(weights, values, W, n, memo);
    }

    private int solveKnapsackMemo(int[] weights, int[] values, int W, int n, int[][] memo) {
        if (n == 0 || W == 0) return 0;
        if (memo[n][W] != -1) return memo[n][W];

        if (weights[n - 1] > W) {
            return memo[n][W] = solveKnapsackMemo(weights, values, W, n - 1, memo);
        }

        int include = values[n - 1] + solveKnapsackMemo(weights, values, W - weights[n - 1], n - 1, memo);
        int exclude = solveKnapsackMemo(weights, values, W, n - 1, memo);

        return memo[n][W] = Math.max(include, exclude);
    }

    // =========================================================================
    // STAGE 3: Bottom-Up DP (Tabulation)
    // Time Complexity:  O(N * W)
    // Space Complexity: O(N * W) 2D Grid
    // =========================================================================
    public int knapsack_Stage3_Tabulation(int[] weights, int[] values, int W) {
        int n = weights.length;
        int[][] dp = new int[n + 1][W + 1];

        for (int i = 1; i <= n; i++) {
            for (int w = 1; w <= W; w++) {
                if (weights[i - 1] <= w) {
                    dp[i][w] = Math.max(values[i - 1] + dp[i - 1][w - weights[i - 1]], dp[i - 1][w]);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }
        return dp[n][W];
    }

    // =========================================================================
    // STAGE 4: Space-Optimized DP (1D Array Traversed Backwards)
    // Time Complexity:  O(N * W)
    // Space Complexity: O(W) - Massive 95%+ RAM reduction!
    // Critical Rule: Inner loop MUST iterate BACKWARDS from W down to weights[i]
    // to prevent using the same item multiple times in the same pass!
    // =========================================================================
    public int knapsack_Stage4_SpaceOptimized(int[] weights, int[] values, int W) {
        int[] dp = new int[W + 1];

        for (int i = 0; i < weights.length; i++) {
            for (int w = W; w >= weights[i]; w--) {
                dp[w] = Math.max(dp[w], values[i] + dp[w - weights[i]]);
            }
        }
        return dp[W];
    }
}
```

---

#### Case Study 3: Longest Common Subsequence (LCS)

* **Problem Statement**: Given two strings `text1` and `text2`, return the length of their longest common subsequence. If there is no common subsequence, return `0`.

```java
package com.leetcode.dp.evolution;

import java.util.Arrays;

public class LCSEvolution {

    // =========================================================================
    // STAGE 1: Naive Recursive (Brute Force)
    // Time Complexity:  O(2^(M + N))
    // Space Complexity: O(M + N) Call Stack
    // =========================================================================
    public int lcs_Stage1_BruteForce(String s1, String s2, int m, int n) {
        if (m == 0 || n == 0) return 0;

        if (s1.charAt(m - 1) == s2.charAt(n - 1)) {
            return 1 + lcs_Stage1_BruteForce(s1, s2, m - 1, n - 1);
        } else {
            return Math.max(lcs_Stage1_BruteForce(s1, s2, m - 1, n),
                            lcs_Stage1_BruteForce(s1, s2, m, n - 1));
        }
    }

    // =========================================================================
    // STAGE 2: Top-Down DP (Memoization)
    // Time Complexity:  O(M * N)
    // Space Complexity: O(M * N) Table + O(M + N) Stack
    // =========================================================================
    public int lcs_Stage2_Memoization(String s1, String s2) {
        int m = s1.length(), n = s2.length();
        int[][] memo = new int[m + 1][n + 1];
        for (int[] row : memo) Arrays.fill(row, -1);
        return solveLCSMemo(s1, s2, m, n, memo);
    }

    private int solveLCSMemo(String s1, String s2, int m, int n, int[][] memo) {
        if (m == 0 || n == 0) return 0;
        if (memo[m][n] != -1) return memo[m][n];

        if (s1.charAt(m - 1) == s2.charAt(n - 1)) {
            return memo[m][n] = 1 + solveLCSMemo(s1, s2, m - 1, n - 1, memo);
        } else {
            return memo[m][n] = Math.max(solveLCSMemo(s1, s2, m - 1, n, memo),
                                         solveLCSMemo(s1, s2, m, n - 1, memo));
        }
    }

    // =========================================================================
    // STAGE 3: Bottom-Up DP (Tabulation)
    // Time Complexity:  O(M * N)
    // Space Complexity: O(M * N)
    // =========================================================================
    public int lcs_Stage3_Tabulation(String s1, String s2) {
        int m = s1.length(), n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[m][n];
    }

    // =========================================================================
    // STAGE 4: Space-Optimized DP (2 Rolling Rows)
    // Time Complexity:  O(M * N)
    // Space Complexity: O(min(M, N)) - Preserves only the previous row
    // =========================================================================
    public int lcs_Stage4_SpaceOptimized(String s1, String s2) {
        if (s1.length() < s2.length()) return lcs_Stage4_SpaceOptimized(s2, s1);
        int m = s1.length(), n = s2.length();
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    curr[j] = 1 + prev[j - 1];
                } else {
                    curr[j] = Math.max(prev[j], curr[j - 1]);
                }
            }
            System.arraycopy(curr, 0, prev, 0, n + 1);
        }
        return prev[n];
    }
}
```

---

#### Case Study 4: Coin Change (Minimum Coins to Make Amount)

* **Problem Statement**: Given an integer array `coins` representing coins of different denominations and an integer `amount`. Return the fewest number of coins that you need to make up that amount. If that amount of money cannot be made up by any combination of the coins, return `-1`.

```java
package com.leetcode.dp.evolution;

import java.util.Arrays;

public class CoinChangeEvolution {

    // =========================================================================
    // STAGE 1: Naive Recursive (Brute Force)
    // Time Complexity:  O(S^N) where S is amount, N is number of coins
    // Space Complexity: O(Amount) Stack depth
    // =========================================================================
    public int coinChange_Stage1_BruteForce(int[] coins, int amount) {
        if (amount == 0) return 0;
        if (amount < 0) return -1;

        int minCoins = Integer.MAX_VALUE;
        for (int coin : coins) {
            int result = coinChange_Stage1_BruteForce(coins, amount - coin);
            if (result >= 0 && result < minCoins) {
                minCoins = 1 + result;
            }
        }
        return minCoins == Integer.MAX_VALUE ? -1 : minCoins;
    }

    // =========================================================================
    // STAGE 2: Top-Down DP (Memoization)
    // Time Complexity:  O(Amount * N)
    // Space Complexity: O(Amount) Memo Array + Stack
    // =========================================================================
    public int coinChange_Stage2_Memoization(int[] coins, int amount) {
        int[] memo = new int[amount + 1];
        return solveCoinChangeMemo(coins, amount, memo);
    }

    private int solveCoinChangeMemo(int[] coins, int amount, int[] memo) {
        if (amount == 0) return 0;
        if (amount < 0) return -1;
        if (memo[amount] != 0) return memo[amount];

        int minCoins = Integer.MAX_VALUE;
        for (int coin : coins) {
            int res = solveCoinChangeMemo(coins, amount - coin, memo);
            if (res >= 0 && res < minCoins) {
                minCoins = 1 + res;
            }
        }
        memo[amount] = (minCoins == Integer.MAX_VALUE) ? -1 : minCoins;
        return memo[amount];
    }

    // =========================================================================
    // STAGE 3 & 4: Bottom-Up DP (1D Tabulation)
    // Time Complexity:  O(Amount * N)
    // Space Complexity: O(Amount)
    // =========================================================================
    public int coinChange_Stage3_Tabulation(int[] coins, int amount) {
        int max = amount + 1;
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, max);
        dp[0] = 0; // 0 coins needed to make amount 0

        for (int i = 1; i <= amount; i++) {
            for (int coin : coins) {
                if (coin <= i) {
                    dp[i] = Math.min(dp[i], dp[i - coin] + 1);
                }
            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }
}
```

## 🧩 Phase 2: The 18 Essential LeetCode Problem-Solving Patterns

---

### Pattern 1: Two Pointers Pattern

```
========================= VISUAL TWO POINTERS BLUEPRINT =========================
1. Convergent / Opposite-End Pointers (Sorted Arrays, Palindromes, Water Trapping):
   Left Pointer ---> [  2,   7,  11,  15,  18,  22  ] <--- Right Pointer
                     ^                                  ^
                     L                                  R
   - If sum < target: Increment L (L++) to increase sum
   - If sum > target: Decrement R (R--) to decrease sum
   - If sum == target: Match found!

2. Same-Direction / Fast-Slow Pointers (Partitioning, In-place array modification):
   [  0,   1,   0,   3,  12  ]
      ^    ^
      S    F   (Slow pointer marks write boundary; Fast pointer scans elements)
=================================================================================
```

#### 🎯 Recognition Signals (When to use Two Pointers):
* The input is a **sorted array** or string, and you need to find a pair, triplet, or subarray satisfying a target sum.
* You need to reverse, inspect palindromes, or rearrange elements in-place with **$O(1)$ auxiliary memory**.
* The naive solution requires nested loops ($O(N^2)$), but shrinking the search space from both ends eliminates one loop dimension down to $O(N)$.

#### 🛠️ Master Reusable Java Template:
```java
public int[] twoPointersConvergentTemplate(int[] nums, int target) {
    int left = 0, right = nums.length - 1;
    while (left < right) {
        int currentSum = nums[left] + nums[right];
        if (currentSum == target) {
            return new int[]{left, right}; // Match found
        } else if (currentSum < target) {
            left++; // Need larger sum -> move left pointer rightward
        } else {
            right--; // Need smaller sum -> move right pointer leftward
        }
    }
    return new int[]{-1, -1}; // Not found
}
```

---

#### Problem 1.1: Two Sum II - Input Array Is Sorted (LeetCode #167) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a 1-indexed array of integers `numbers` that is already sorted in non-decreasing order, find two numbers such that they add up to a specific `target` number. Return the indices of the two numbers, added by one, as an integer array `[index1, index2]` of length 2.
* **Constraints**:
  - $2 \le \text{numbers.length} \le 3 \times 10^4$
  - $-1000 \le \text{numbers}[i] \le 1000$
  - `numbers` is sorted in non-decreasing order.
  - Tests are generated such that there is exactly one solution.
  - You may not use the same element twice. Must use only $O(1)$ extra space.

##### 2. 👁️ Visual Execution Trace
```
Target = 9, Array = [ 2,  7, 11, 15 ]
Step 1: L=0 (val=2), R=3 (val=15) -> Sum = 2 + 15 = 17 (> 9) -> Sum too large! Decrement R (R=2).
Step 2: L=0 (val=2), R=2 (val=11) -> Sum = 2 + 11 = 13 (> 9) -> Sum too large! Decrement R (R=1).
Step 3: L=0 (val=2), R=1 (val=7)  -> Sum = 2 + 7  = 9  (== 9) -> Match found! Return [1, 2] (1-indexed).
```

##### 3. 🐢 Approach 1: Naive Nested Loops (Brute Force)
* **Logic**: Check every possible pair $(i, j)$ where $i < j$.
```java
public int[] twoSum_BruteForce(int[] numbers, int target) {
    for (int i = 0; i < numbers.length; i++) {
        for (int j = i + 1; j < numbers.length; j++) {
            if (numbers[i] + numbers[j] == target) {
                return new int[]{i + 1, j + 1};
            }
        }
    }
    return new int[]{-1, -1};
}
// Time Complexity: O(N^2) - Causes TLE for N = 30,000. Space Complexity: O(1).
```

##### 4. ⚖️ Approach 2: Binary Search
* **Logic**: For each element `numbers[i]`, use binary search to locate `target - numbers[i]` in the subarray `[i + 1 ... N - 1]`.
```java
public int[] twoSum_BinarySearch(int[] numbers, int target) {
    for (int i = 0; i < numbers.length; i++) {
        int complement = target - numbers[i];
        int low = i + 1, high = numbers.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (numbers[mid] == complement) return new int[]{i + 1, mid + 1};
            else if (numbers[mid] < complement) low = mid + 1;
            else high = mid - 1;
        }
    }
    return new int[]{-1, -1};
}
// Time Complexity: O(N log N). Space Complexity: O(1).
```

##### 5. ⚡ Approach 3: Optimal Two Pointers (Gold Standard)
* **Core Insight**: Since the array is sorted, `numbers[left] + numbers[right]` provides a monotonic monotonic sum. If the sum is too small, only advancing `left` can increase it; if too large, only retreating `right` can decrease it.

```java
package com.leetcode.twopointers;

public class TwoSumSorted {
    public int[] twoSum(int[] numbers, int target) {
        int left = 0;
        int right = numbers.length - 1;

        while (left < right) {
            int sum = numbers[left] + numbers[right];
            if (sum == target) {
                return new int[]{left + 1, right + 1}; // 1-indexed result
            } else if (sum < target) {
                left++;  // Move left pointer rightward to increase sum
            } else {
                right--; // Move right pointer leftward to decrease sum
            }
        }
        return new int[]{-1, -1};
    }
}
```

##### 6. 📊 Comparative Solution Analysis & Trade-off Matrix
| Approach | Time Complexity | Space Complexity | Pros | Cons / Verdict |
| :--- | :--- | :--- | :--- | :--- |
| **1. Brute Force** | $O(N^2)$ | $O(1)$ | No memory overhead | TLE on $N = 30,000$ |
| **2. Binary Search**| $O(N \log N)$ | $O(1)$ | Faster than brute force | $N$ binary searches overhead |
| **3. Two Pointers** | $O(N)$ | $O(1)$ | Single linear pass, optimal | Requires sorted input |

---

#### Problem 1.2: 3Sum (LeetCode #15) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums`, return all the triplets `[nums[i], nums[j], nums[k]]` such that $i \neq j, i \neq k, \text{ and } j \neq k$, and `nums[i] + nums[j] + nums[k] == 0`. Notice that the solution set must not contain duplicate triplets.
* **Constraints**:
  - $3 \le \text{nums.length} \le 3000$
  - $-10^5 \le \text{nums}[i] \le 10^5$

##### 2. 👁️ Visual Execution Trace
```
Sorted Array = [ -4, -1, -1,  0,  1,  2 ]
Fix i=0 (val=-4): Need two numbers summing to +4. L=1 (-1), R=5 (2) -> sum=-4+(-1)+2 = -3 (<0) -> L++ ... No match.
Fix i=1 (val=-1): Need sum to +1. L=2 (-1), R=5 (2) -> sum=-1+(-1)+2 = 0 == 0! Found: [-1, -1, 2].
                  Skip duplicates for L and R: L->3 (0), R->4 (1) -> sum=-1+0+1 = 0! Found: [-1, 0, 1].
Fix i=2 (val=-1): Duplicate of i=1! Skip to avoid duplicate triplet.
```

##### 3. 🐢 Approach 1: Naive 3-Loop Brute Force
* **Logic**: Three nested loops checking all $(i, j, k)$ triplets, inserting sorted triplets into a `HashSet` to deduplicate.
```java
// Time Complexity: O(N^3). Space Complexity: O(N) for HashSet. Causes extreme TLE.
```

##### 4. ⚖️ Approach 2: Hash Map with Outer Loop
* **Logic**: Fix `nums[i]`, then run 2Sum with a Hash Set for the remaining elements. Still requires complex sorting and hashing to prevent duplicate triplets.
```java
// Time Complexity: O(N^2). Space Complexity: O(N) extra memory.
```

##### 5. ⚡ Approach 3: Sort + Two Pointers (Gold Standard)
* **Core Insight**: Sort array in $O(N \log N)$. Fix element $i$ with a loop, and solve 2Sum on the remaining subarray with Two Pointers in $O(N)$. Total time: $O(N^2)$. Easily skip duplicate values by advancing pointers while `nums[p] == nums[p-1]`.

```java
package com.leetcode.twopointers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThreeSum {
    public List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums); // O(N log N)

        for (int i = 0; i < nums.length - 2; i++) {
            // Early exit: if smallest element > 0, three positive numbers cannot sum to 0
            if (nums[i] > 0) break;

            // Skip duplicate outer elements to guarantee unique triplets
            if (i > 0 && nums[i] == nums[i - 1]) continue;

            int left = i + 1;
            int right = nums.length - 1;

            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];

                if (sum == 0) {
                    result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                    left++;
                    right--;

                    // Skip duplicate left and right elements
                    while (left < right && nums[left] == nums[left - 1]) left++;
                    while (left < right && nums[right] == nums[right + 1]) right--;
                } else if (sum < 0) {
                    left++;
                } else {
                    right--;
                }
            }
        }
        return result;
    }
}
```

##### 6. 📊 Comparative Solution Analysis & Trade-off Matrix
| Approach | Time Complexity | Space Complexity | Pros | Cons / Verdict |
| :--- | :--- | :--- | :--- | :--- |
| **1. Brute Force** | $O(N^3)$ | $O(N)$ | Simple logic | Exponentially slow, TLE |
| **2. Hash Set** | $O(N^2)$ | $O(N)$ | Eliminates 1 loop | Heavy hashing & Set overhead |
| **3. Sort + 2 Pointers**| $O(N^2)$ | $O(1)$ / $O(\log N)$ | Zero extra space, fast deduplication | Requires array mutation |

---

#### Problem 1.3: 4Sum (LeetCode #18) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array `nums` of $N$ integers and an integer `target`, return an array of all the unique quadruplets `[nums[a], nums[b], nums[c], nums[d]]` such that $a, b, c, d$ are distinct and `nums[a] + nums[b] + nums[c] + nums[d] == target`.
* **Constraints**: $1 \le \text{nums.length} \le 200$, $-10^9 \le \text{nums}[i], \text{target} \le 10^9$. Watch out for 32-bit integer overflow!

##### 2. ⚡ Optimal Solution: General $K$-Sum / Nested Two Pointers
```java
package com.leetcode.twopointers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FourSum {
    public List<List<Integer>> fourSum(int[] nums, int target) {
        List<List<Integer>> result = new ArrayList<>();
        if (nums == null || nums.length < 4) return result;
        Arrays.sort(nums);

        int n = nums.length;
        for (int i = 0; i < n - 3; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) continue; // Skip duplicate i

            for (int j = i + 1; j < n - 2; j++) {
                if (j > i + 1 && nums[j] == nums[j - 1]) continue; // Skip duplicate j

                int left = j + 1;
                int right = n - 1;

                while (left < right) {
                    // Use long to prevent 32-bit integer overflow during summation
                    long sum = (long) nums[i] + nums[j] + nums[left] + nums[right];

                    if (sum == target) {
                        result.add(Arrays.asList(nums[i], nums[j], nums[left], nums[right]));
                        left++;
                        right--;
                        while (left < right && nums[left] == nums[left - 1]) left++;
                        while (left < right && nums[right] == nums[right + 1]) right--;
                    } else if (sum < target) {
                        left++;
                    } else {
                        right--;
                    }
                }
            }
        }
        return result;
    }
}
// Time Complexity: O(N^3). Space Complexity: O(1) auxiliary.
```

---

#### Problem 1.4: Container With Most Water (LeetCode #11) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `height` of length $N$. There are $N$ vertical lines drawn such that the two endpoints of the $i$-th line are $(i, 0)$ and $(i, \text{height}[i])$. Find two lines that together with the x-axis form a container, such that the container contains the most water. Return the maximum amount of water a container can store.
* **Constraints**: $2 \le N \le 10^5$, $0 \le \text{height}[i] \le 10^4$.

##### 2. 👁️ Visual Execution Trace
```
height = [1, 8, 6, 2, 5, 4, 8, 3, 7]
Width = R - L, Area = Width * min(height[L], height[R])

Initial: L=0 (h=1), R=8 (h=7) -> Width=8, Area = 8 * min(1, 7) = 8.
Insight: Moving R leftward CANNOT increase area (width shrinks, height capped at 1).
         We MUST move the shorter line (L=0 -> L=1) to seek a taller barrier!
Step 2:  L=1 (h=8), R=8 (h=7) -> Width=7, Area = 7 * min(8, 7) = 49 (New Max!)
Step 3:  R=8 is shorter (7 < 8) -> Move R leftward (R=7).
```

##### 3. ⚡ Optimal Two Pointers Solution
```java
package com.leetcode.twopointers;

public class ContainerWithMostWater {
    public int maxArea(int[] height) {
        int maxWater = 0;
        int left = 0;
        int right = height.length - 1;

        while (left < right) {
            int width = right - left;
            int currentHeight = Math.min(height[left], height[right]);
            maxWater = Math.max(maxWater, width * currentHeight);

            // Move the pointer with the smaller height inwards
            if (height[left] < height[right]) {
                left++;
            } else {
                right--;
            }
        }
        return maxWater;
    }
}
// Time Complexity: O(N) single pass. Space Complexity: O(1).
```

##### 4. 📊 Comparative Trade-off Matrix
| Approach | Time Complexity | Space Complexity | Why It Holds |
| :--- | :--- | :--- | :--- |
| **Brute Force (All pairs)** | $O(N^2)$ | $O(1)$ | Evaluates all $N(N-1)/2$ combinations. TLE on $N = 10^5$. |
| **Two Pointers (Optimal)** | $O(N)$ | $O(1)$ | Prunes sub-optimal rectangles in $O(1)$ per step. |

---

#### Problem 1.5: Trapping Rain Water (LeetCode #42) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given $N$ non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining.
* **Constraints**: $1 \le N \le 2 \times 10^4$, $0 \le \text{height}[i] \le 10^5$.

##### 2. 👁️ Visual Execution Trace
```
Elevation: [ 0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1 ]
Water trapped at index i = max(0, min(maxLeft, maxRight) - height[i])

With Two Pointers:
- Maintain `leftMax` and `rightMax`.
- If `height[left] < height[right]`: Water at `left` is strictly bounded by `leftMax` (since `leftMax < rightMax`). Accumulate water and increment `left`.
- Else: Water at `right` is strictly bounded by `rightMax`. Accumulate water and decrement `right`.
```

##### 3. ⚡ Optimal Solution (Two Pointers in $O(1)$ Space)
```java
package com.leetcode.twopointers;

public class TrappingRainWater {
    public int trap(int[] height) {
        if (height == null || height.length < 3) return 0;

        int left = 0, right = height.length - 1;
        int leftMax = 0, rightMax = 0;
        int totalWater = 0;

        while (left < right) {
            if (height[left] < height[right]) {
                if (height[left] >= leftMax) {
                    leftMax = height[left];
                } else {
                    totalWater += leftMax - height[left];
                }
                left++;
            } else {
                if (height[right] >= rightMax) {
                    rightMax = height[right];
                } else {
                    totalWater += rightMax - height[right];
                }
                right--;
            }
        }
        return totalWater;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) auxiliary (Beats Dynamic Programming O(N) space).
```

---

#### Problem 1.6: Valid Palindrome (LeetCode #125) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: A phrase is a palindrome if, after converting all uppercase letters into lowercase letters and removing all non-alphanumeric characters, it reads the same forward and backward. Return `true` if it is a palindrome, or `false` otherwise.
* **Constraints**: $1 \le s\text{.length} \le 2 \times 10^5$.

##### 2. ⚡ Optimal Solution (In-place Two Pointers without Regex/Extra Strings)
```java
package com.leetcode.twopointers;

public class ValidPalindrome {
    public boolean isPalindrome(String s) {
        int left = 0, right = s.length() - 1;

        while (left < right) {
            // Skip non-alphanumeric characters from left
            while (left < right && !Character.isLetterOrDigit(s.charAt(left))) {
                left++;
            }
            // Skip non-alphanumeric characters from right
            while (left < right && !Character.isLetterOrDigit(s.charAt(right))) {
                right--;
            }

            // Compare case-insensitively
            if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) in-place.
```

---

#### Problem 1.7: Remove Duplicates from Sorted Array (LeetCode #26) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums` sorted in non-decreasing order, remove duplicates in-place such that each unique element appears only once. Return the number of unique elements $k$.
* **Constraints**: $1 \le \text{nums.length} \le 3 \times 10^4$. Must modify array in-place with $O(1)$ extra memory.

##### 2. ⚡ Optimal Solution (Fast & Slow Same-Direction Pointers)
```java
package com.leetcode.twopointers;

public class RemoveDuplicatesSortedArray {
    public int removeDuplicates(int[] nums) {
        if (nums.length == 0) return 0;

        int slow = 0; // Marks boundary of unique elements

        for (int fast = 1; fast < nums.length; fast++) {
            if (nums[fast] != nums[slow]) {
                slow++;
                nums[slow] = nums[fast]; // Write unique element forward
            }
        }
        return slow + 1;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 1.8: Move Zeroes (LeetCode #283) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums`, move all `0`'s to the end of it while maintaining the relative order of the non-zero elements. Must do this in-place.
* **Constraints**: $1 \le \text{nums.length} \le 10^4$.

##### 2. ⚡ Optimal Solution (Partitioning Two Pointers)
```java
package com.leetcode.twopointers;

public class MoveZeroes {
    public void moveZeroes(int[] nums) {
        int insertPos = 0; // Points to next available position for a non-zero element

        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                // Swap non-zero element to insertPos
                int temp = nums[insertPos];
                nums[insertPos] = nums[i];
                nums[i] = temp;
                insertPos++;
            }
        }
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 1.9: Sort Colors / Dutch National Flag (LeetCode #75) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array `nums` with $N$ objects colored red (0), white (1), or blue (2), sort them in-place so that objects of the same color are adjacent, in order 0, 1, 2. Must solve in a single pass without using library sort.
* **Constraints**: $1 \le N \le 300$, `nums[i]` is 0, 1, or 2.

##### 2. 👁️ Visual Execution Trace (3-Way Partitioning)
```
Pointers: `low` (boundary for 0s), `mid` (current scanner), `high` (boundary for 2s)
- If nums[mid] == 0: swap(nums[low], nums[mid]), low++, mid++
- If nums[mid] == 1: mid++
- If nums[mid] == 2: swap(nums[mid], nums[high]), high-- (do NOT advance mid, must check swapped element!)
```

##### 3. ⚡ Optimal Solution (Dutch National Flag Algorithm)
```java
package com.leetcode.twopointers;

public class SortColors {
    public void sortColors(int[] nums) {
        int low = 0;
        int mid = 0;
        int high = nums.length - 1;

        while (mid <= high) {
            if (nums[mid] == 0) {
                swap(nums, low, mid);
                low++;
                mid++;
            } else if (nums[mid] == 1) {
                mid++;
            } else { // nums[mid] == 2
                swap(nums, mid, high);
                high--;
            }
        }
    }

    private void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
}
// Time Complexity: O(N) single pass. Space Complexity: O(1).
```

---

#### Problem 1.10: Boats to Save People (LeetCode #881) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an array `people` where `people[i]` is the weight of the $i$-th person, and an infinite number of boats where each boat can carry at most `limit` weight. Each boat carries at most **two people** at the same time. Return the minimum number of boats to carry every given person.
* **Constraints**: $1 \le \text{people.length} \le 5 \times 10^4$, $1 \le \text{people}[i] \le \text{limit} \le 3 \times 10^4$.

##### 2. ⚡ Optimal Greedy Two Pointers Solution
```java
package com.leetcode.twopointers;

import java.util.Arrays;

public class BoatsToSavePeople {
    public int numRescueBoats(int[] people, int limit) {
        Arrays.sort(people); // O(N log N)
        int left = 0;                 // Lightest person
        int right = people.length - 1; // Heaviest person
        int boats = 0;

        while (left <= right) {
            // If the lightest and heaviest person can share a boat, pair them
            if (people[left] + people[right] <= limit) {
                left++;
            }
            // In all cases, the heaviest person gets a boat
            right--;
            boats++;
        }
        return boats;
    }
}
// Time Complexity: O(N log N) due to sorting. Space Complexity: O(1) or O(log N).
```

### Pattern 2: Sliding Window Pattern

```
========================= VISUAL SLIDING WINDOW BLUEPRINT =========================
1. Fixed-Size Window (Window of exact size K):
   Array: [ 2,  1,  5,  1,  3,  2 ],  K = 3
   Pass 1: [ 2,  1,  5 ] -> Sum = 8
   Pass 2:     [ 1,  5,  1 ] -> Sum = 8 - 2 + 1 = 7 (Slide: subtract outgoing, add incoming)
   Pass 3:         [ 5,  1,  3 ] -> Sum = 7 - 1 + 3 = 9 (Max Sum = 9)

2. Dynamic-Size / Shrinkable Window (Longest/Shortest Subarray satisfying Condition):
   [ a,  b,  c,  a,  b,  c,  b,  b ]
     ^           ^
     L           R  (Expand R to add elements; When condition violates, shrink L++)
===================================================================================
```

#### 🎯 Recognition Signals (When to use Sliding Window):
* The problem asks for the **longest, shortest, or target contiguous subarray or substring** (e.g. max sum of size $K$, longest substring with distinct characters).
* Computing properties of subarray $[i \dots j]$ can be easily updated in $O(1)$ from $[i-1 \dots j-1]$ by adding incoming element at $j$ and subtracting outgoing element at $i-1$.
* Replaces naive $O(N^2)$ / $O(N^3)$ nested subarray generation with a single linear $O(N)$ pass.

#### 🛠️ Master Reusable Java Template:
```java
public int dynamicSlidingWindowTemplate(String s) {
    int left = 0, maxLength = 0;
    Map<Character, Integer> freqMap = new HashMap<>();

    for (int right = 0; right < s.length(); right++) {
        char rightChar = s.charAt(right);
        freqMap.put(rightChar, freqMap.getOrDefault(rightChar, 0) + 1);

        // While window is INVALID according to problem constraints -> shrink from left
        while (!isValidWindow(freqMap)) {
            char leftChar = s.charAt(left);
            freqMap.put(leftChar, freqMap.get(leftChar) - 1);
            if (freqMap.get(leftChar) == 0) freqMap.remove(leftChar);
            left++; // Shrink window
        }

        // Window is valid here -> record optimal metric
        maxLength = Math.max(maxLength, right - left + 1);
    }
    return maxLength;
}
```

---

#### Problem 2.1: Longest Substring Without Repeating Characters (LeetCode #3) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a string `s`, find the length of the longest substring without duplicate characters.
* **Constraints**: $0 \le s\text{.length} \le 5 \times 10^4$. `s` consists of English letters, digits, symbols and spaces.

##### 2. 👁️ Visual Execution Trace
```
s = "abcabcbb"
R=0: 'a' -> window "a", len=1. Map={'a':0}
R=1: 'b' -> window "ab", len=2. Map={'a':0, 'b':1}
R=2: 'c' -> window "abc", len=3. Map={'a':0, 'b':1, 'c':2}
R=3: 'a' -> Duplicate 'a' found at idx 0! Jump Left pointer to 0 + 1 = 1. Window "bca", len=3.
R=4: 'b' -> Duplicate 'b' found at idx 1! Jump Left pointer to 1 + 1 = 2. Window "cab", len=3.
... Max Length = 3 ("abc").
```

##### 3. 🐢 Approach 1: Brute Force (Generate All Substrings)
* **Logic**: Check all $O(N^2)$ substrings, and test if each has distinct characters using a Set in $O(N)$.
```java
// Time Complexity: O(N^3). Space Complexity: O(min(N, M)) where M is character set. TLE on N=50,000.
```

##### 4. ⚡ Approach 2 & 3: Sliding Window with Last-Seen Index Array (Gold Standard)
```java
package com.leetcode.slidingwindow;

import java.util.Arrays;

public class LongestSubstringWithoutRepeating {
    public int lengthOfLongestSubstring(String s) {
        if (s == null || s.length() == 0) return 0;

        // Store last seen index of ASCII characters (direct array lookup is 5x faster than HashMap)
        int[] lastIndex = new int[128];
        Arrays.fill(lastIndex, -1);

        int maxLength = 0;
        int left = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);

            // If character was seen inside current window, jump left pointer past it
            if (lastIndex[c] >= left) {
                left = lastIndex[c] + 1;
            }

            lastIndex[c] = right; // Update last seen index
            maxLength = Math.max(maxLength, right - left + 1);
        }
        return maxLength;
    }
}
// Time Complexity: O(N) single pass. Space Complexity: O(1) fixed 128-element array.
```

##### 5. 📊 Comparative Trade-off Matrix
| Approach | Time Complexity | Space Complexity | Pros | Cons / Verdict |
| :--- | :--- | :--- | :--- | :--- |
| **1. Brute Force** | $O(N^3)$ | $O(N)$ | Simple brute force | Unusable on large $N$, TLE |
| **2. HashMap Window**| $O(N)$ | $O(\min(N, 128))$ | Dynamic size handling | Boxing & hashing overhead |
| **3. Array Window** | $O(N)$ | $O(1)$ constant | Cache-friendly, $<2\text{ms}$ execution | Fixed ASCII charset |

---

#### Problem 2.2: Minimum Window Substring (LeetCode #76) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given two strings `s` and `t` of lengths $m$ and $n$ respectively, return the minimum window substring of `s` such that every character in `t` (including duplicates) is included in the window. If there is no such substring, return the empty string `""`.
* **Constraints**: $1 \le m, n \le 10^5$. `s` and `t` consist of uppercase and lowercase English letters.

##### 2. 👁️ Visual Execution Trace
```
s = "ADOBECODEBANC", t = "ABC"
Target map: {'A':1, 'B':1, 'C':1}. Required matches = 3.
Expand R until window contains all characters:
- At R=5 ("ADOBEC"), matched=3 (all present). Window valid! Len=6.
- Try shrinking L: L=0 ('A') removed -> matched drops to 2 (invalid). Stop shrink.
- Expand R again: R=10 ("DOBECODEBA"), matched=3. Shrink L: "CODEBA" -> "ODEBANC" -> "BANC" (Len=4, New Min!).
```

##### 3. ⚡ Optimal Sliding Window Solution
```java
package com.leetcode.slidingwindow;

public class MinimumWindowSubstring {
    public String minWindow(String s, String t) {
        if (s.length() < t.length()) return "";

        int[] targetFreq = new int[128];
        for (char c : t.toCharArray()) {
            targetFreq[c]++;
        }

        int[] windowFreq = new int[128];
        int left = 0, matchedCount = 0, requiredCount = t.length();
        int minLen = Integer.MAX_VALUE, startIndex = 0;

        for (int right = 0; right < s.length(); right++) {
            char rightChar = s.charAt(right);
            windowFreq[rightChar]++;

            // If current char is needed by target string t, increment matchedCount
            if (targetFreq[rightChar] > 0 && windowFreq[rightChar] <= targetFreq[rightChar]) {
                matchedCount++;
            }

            // When all characters of t are matched, try shrinking the window from left
            while (matchedCount == requiredCount) {
                if (right - left + 1 < minLen) {
                    minLen = right - left + 1;
                    startIndex = left;
                }

                char leftChar = s.charAt(left);
                windowFreq[leftChar]--;
                if (targetFreq[leftChar] > 0 && windowFreq[leftChar] < targetFreq[leftChar]) {
                    matchedCount--; // Window lost a required character
                }
                left++;
            }
        }

        return minLen == Integer.MAX_VALUE ? "" : s.substring(startIndex, startIndex + minLen);
    }
}
// Time Complexity: O(M + N). Space Complexity: O(1) 128-element arrays.
```

---

#### Problem 2.3: Longest Repeating Character Replacement (LeetCode #424) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given a string `s` and an integer `k`. You can choose any character of the string and change it to any other uppercase English character at most `k` times. Return the length of the longest substring containing the same letter you can get after performing at most `k` operations.
* **Constraints**: $1 \le s\text{.length} \le 10^5$, $0 \le k \le s\text{.length}$. `s` consists of only uppercase English letters.

##### 2. ⚡ Optimal Solution: Sliding Window with Max Frequency Tracking
```java
package com.leetcode.slidingwindow;

public class CharacterReplacement {
    public int characterReplacement(String s, int k) {
        int[] freq = new int[26];
        int left = 0, maxFreq = 0, maxLength = 0;

        for (int right = 0; right < s.length(); right++) {
            int charIdx = s.charAt(right) - 'A';
            freq[charIdx]++;
            maxFreq = Math.max(maxFreq, freq[charIdx]);

            // Number of characters to replace = (window length - maxFreq)
            // If replacements needed > k, shrink window from left
            if ((right - left + 1) - maxFreq > k) {
                freq[s.charAt(left) - 'A']--;
                left++;
            }

            maxLength = Math.max(maxLength, right - left + 1);
        }
        return maxLength;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) 26-element array.
```

---

#### Problem 2.4: Max Consecutive Ones III (LeetCode #1004) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a binary array `nums` and an integer `k`, return the maximum number of consecutive `1`'s in the array if you can flip at most `k` `0`'s.
* **Constraints**: $1 \le \text{nums.length} \le 10^5$, `nums[i]` is either 0 or 1, $0 \le k \le \text{nums.length}$.

##### 2. ⚡ Optimal Sliding Window Solution
```java
package com.leetcode.slidingwindow;

public class MaxConsecutiveOnesIII {
    public int longestOnes(int[] nums, int k) {
        int left = 0, zeroCount = 0, maxConsecutive = 0;

        for (int right = 0; right < nums.length; right++) {
            if (nums[right] == 0) {
                zeroCount++;
            }

            // If number of flipped zeros exceeds k, slide left boundary
            while (zeroCount > k) {
                if (nums[left] == 0) {
                    zeroCount--;
                }
                left++;
            }

            maxConsecutive = Math.max(maxConsecutive, right - left + 1);
        }
        return maxConsecutive;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 2.5: Sliding Window Maximum (LeetCode #239) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an array of integers `nums`, there is a sliding window of size `k` which is moving from the very left of the array to the very right. You can only see the `k` numbers in the window. Each time the sliding window moves right by one position. Return the max sliding window.
* **Constraints**: $1 \le \text{nums.length} \le 10^5$, $1 \le k \le \text{nums.length}$.

##### 2. 👁️ Visual Execution Trace (Monotonic Decreasing Deque)
```
nums = [ 1,  3, -1, -3,  5,  3,  6,  7 ], k = 3
Deque stores INDICES in monotonic decreasing order of values:
i=0 (1): Deque=[0] (val:1)
i=1 (3): 3 > 1 -> Pop 0. Deque=[1] (val:3)
i=2 (-1): -1 < 3 -> Deque=[1, 2] (vals:3, -1). Window full (k=3) -> Max = nums[Deque.peek()] = 3.
i=3 (-3): Deque=[1, 2, 3] (vals:3, -1, -3). Max = 3.
i=4 (5): 5 > -3, -1, 3 -> Pop all! Deque=[4] (val:5). Max = 5.
```

##### 3. ⚡ Optimal Solution (Monotonic Deque in $O(N)$ Time)
```java
package com.leetcode.slidingwindow;

import java.util.ArrayDeque;
import java.util.Deque;

public class SlidingWindowMaximum {
    public int[] maxSlidingWindow(int[] nums, int k) {
        if (nums == null || nums.length == 0 || k <= 0) return new int[0];

        int n = nums.length;
        int[] result = new int[n - k + 1];
        int resultIdx = 0;
        Deque<Integer> deque = new ArrayDeque<>(); // Stores indices

        for (int i = 0; i < n; i++) {
            // 1. Remove indices that are out of current window bounds [i - k + 1, i]
            while (!deque.isEmpty() && deque.peekFirst() < i - k + 1) {
                deque.pollFirst();
            }

            // 2. Maintain monotonic decreasing property: remove smaller elements from back
            while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i]) {
                deque.pollLast();
            }

            // 3. Add current element's index
            deque.offerLast(i);

            // 4. Record current window maximum once window reaches size k
            if (i >= k - 1) {
                result[resultIdx++] = nums[deque.peekFirst()];
            }
        }
        return result;
    }
}
// Time Complexity: O(N) - Each element is pushed and popped at most ONCE. Space Complexity: O(K).
```

---

#### Problem 2.6: Minimum Size Subarray Sum (LeetCode #209) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of positive integers `nums` and a positive integer `target`, return the minimal length of a subarray whose sum is greater than or equal to `target`. If there is no such subarray, return `0` instead.
* **Constraints**: $1 \le \text{target} \le 10^9$, $1 \le \text{nums.length} \le 10^5$, $1 \le \text{nums}[i] \le 10^4$.

##### 2. ⚡ Optimal Solution (Shrinking Sliding Window)
```java
package com.leetcode.slidingwindow;

public class MinSizeSubarraySum {
    public int minSubArrayLen(int target, int[] nums) {
        int left = 0, currentSum = 0;
        int minLen = Integer.MAX_VALUE;

        for (int right = 0; right < nums.length; right++) {
            currentSum += nums[right];

            // When sum meets or exceeds target, shrink window from left to minimize length
            while (currentSum >= target) {
                minLen = Math.min(minLen, right - left + 1);
                currentSum -= nums[left];
                left++;
            }
        }
        return minLen == Integer.MAX_VALUE ? 0 : minLen;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 2.7: Permutation in String (LeetCode #567) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given two strings `s1` and `s2`, return `true` if `s2` contains a permutation of `s1`, or `false` otherwise. In other words, return `true` if one of `s1`'s permutations is the substring of `s2`.
* **Constraints**: $1 \le s1\text{.length}, s2\text{.length} \le 10^4$. `s1` and `s2` consist of lowercase English letters.

##### 2. ⚡ Optimal Solution (Fixed-Size Window of Length $s1.\text{length}$)
```java
package com.leetcode.slidingwindow;

import java.util.Arrays;

public class PermutationInString {
    public boolean checkInclusion(String s1, String s2) {
        if (s1.length() > s2.length()) return false;

        int[] count1 = new int[26];
        int[] count2 = new int[26];

        int len1 = s1.length();
        for (int i = 0; i < len1; i++) {
            count1[s1.charAt(i) - 'A']++;
            count2[s2.charAt(i) - 'A']++;
        }

        if (Arrays.equals(count1, count2)) return true;

        // Slide window of fixed length `len1` across s2
        for (int i = len1; i < s2.length(); i++) {
            count2[s2.charAt(i) - 'A']++;          // Add incoming char
            count2[s2.charAt(i - len1) - 'A']--;   // Remove outgoing char

            if (Arrays.equals(count1, count2)) return true;
        }
        return false;
    }
}
// Time Complexity: O(26 * (N2 - N1)) = O(N2). Space Complexity: O(1) fixed arrays.
```

---

#### Problem 2.8: Find All Anagrams in a String (LeetCode #438) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given two strings `s` and `p`, return an array of all the start indices of `p`'s anagrams in `s`.
* **Constraints**: $1 \le s\text{.length}, p\text{.length} \le 3 \times 10^4$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.slidingwindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindAllAnagrams {
    public List<Integer> findAnagrams(String s, String p) {
        List<Integer> result = new ArrayList<>();
        if (s.length() < p.length()) return result;

        int[] pFreq = new int[26];
        int[] sFreq = new int[26];

        for (char c : p.toCharArray()) pFreq[c - 'A']++;

        int pLen = p.length();
        for (int i = 0; i < s.length(); i++) {
            sFreq[s.charAt(i) - 'A']++;

            // When window exceeds pLen, remove leftmost character
            if (i >= pLen) {
                sFreq[s.charAt(i - pLen) - 'A']--;
            }

            // If frequency signatures match, record starting index
            if (Arrays.equals(pFreq, sFreq)) {
                result.add(i - pLen + 1);
            }
        }
        return result;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 2.9: Subarray Product Less Than K (LeetCode #713) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of integers `nums` and an integer `k`, return the number of contiguous subarrays where the product of all the elements in the subarray is strictly less than `k`.
* **Constraints**: $1 \le \text{nums.length} \le 3 \times 10^4$, $1 \le \text{nums}[i] \le 1000$, $0 \le k \le 10^6$.

##### 2. ⚡ Optimal Solution: Sliding Window Subarray Counting
```java
package com.leetcode.slidingwindow;

public class SubarrayProductLessThanK {
    public int numSubarrayProductLessThanK(int[] nums, int k) {
        if (k <= 1) return 0; // Since nums[i] >= 1, product can never be < 1

        int product = 1;
        int left = 0;
        int count = 0;

        for (int right = 0; right < nums.length; right++) {
            product *= nums[right];

            while (product >= k && left <= right) {
                product /= nums[left];
                left++;
            }

            // Key Mathematical Invariant:
            // Every new element at `right` contributes exactly `(right - left + 1)` valid subarrays ending at `right`!
            count += (right - left + 1);
        }
        return count;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 2.10: Fruit Into Baskets (LeetCode #904) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are visiting a farm that has a single row of fruit trees arranged from left to right. You have **2 baskets**, and each basket can only hold a **single type** of fruit. Find the maximum total number of fruits you can pick in a single contiguous stretch.
* **Equivalent LeetCode Abstract Problem**: Find the length of the longest contiguous subarray that contains at most **2 distinct integers**.
* **Constraints**: $1 \le \text{fruits.length} \le 10^5$, $0 \le \text{fruits}[i] < \text{fruits.length}$.

##### 2. ⚡ Optimal Solution (Sliding Window with at most 2 Distinct Elements)
```java
package com.leetcode.slidingwindow;

import java.util.HashMap;
import java.util.Map;

public class FruitIntoBaskets {
    public int totalFruit(int[] fruits) {
        Map<Integer, Integer> basket = new HashMap<>(); // Fruit Type -> Count
        int left = 0;
        int maxFruits = 0;

        for (int right = 0; right < fruits.length; right++) {
            basket.put(fruits[right], basket.getOrDefault(fruits[right], 0) + 1);

            // If we have picked more than 2 distinct fruit types, shrink window from left
            while (basket.size() > 2) {
                int leftFruit = fruits[left];
                basket.put(leftFruit, basket.get(leftFruit) - 1);
                if (basket.get(leftFruit) == 0) {
                    basket.remove(leftFruit);
                }
                left++;
            }

            maxFruits = Math.max(maxFruits, right - left + 1);
        }
        return maxFruits;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) since HashMap contains at most 3 entries.
```

### Pattern 3: Fast & Slow Pointers (Floyd's Tortoise and Hare)

```
===================== VISUAL FAST & SLOW POINTERS BLUEPRINT =====================
1. Cycle Detection (Tortoise and Hare):
   Head -> [ 1 ] -> [ 2 ] -> [ 3 ] -> [ 4 ] -> [ 5 ]
                              ^                 |
                              |                 v
                            [ 8 ] <--- [ 7 ] <- [ 6 ]
   - Slow moves 1 step: Slow = Slow.next
   - Fast moves 2 steps: Fast = Fast.next.next
   - Mathematical Guarantee: In a cycle of length C, the distance between Fast and Slow
     decreases by 1 in every iteration. They MUST collide in at most C steps!

2. Middle of Linked List:
   Odd length:  [ 1 ] -> [ 2 ] -> [ 3 ] -> [ 4 ] -> [ 5 ] -> null
                                    ^                 ^
                                   Slow              Fast (Fast.next == null)
   Even length: [ 1 ] -> [ 2 ] -> [ 3 ] -> [ 4 ] -> [ 5 ] -> [ 6 ] -> null
                                             ^                        ^
                                            Slow                     Fast (Fast == null)
=================================================================================
```

#### 🎯 Recognition Signals (When to use Fast & Slow Pointers):
* Linked list cycle detection or finding the start node of a loop.
* Finding the **middle node** of a linked list in a single pass without computing total length first.
* Detecting cycles in arrays / state transitions where values point to the next index (e.g. `Happy Number`, `Find the Duplicate Number`).
* Replaces naive $O(N)$ memory approaches (using `HashSet<ListNode>` to store visited pointers) with **$O(1)$ auxiliary space**.

#### 🛠️ Master Reusable Java Template:
```java
public boolean hasCycleTemplate(ListNode head) {
    if (head == null || head.next == null) return false;

    ListNode slow = head;
    ListNode fast = head;

    while (fast != null && fast.next != null) {
        slow = slow.next;         // 1 step
        fast = fast.next.next;    // 2 steps

        if (slow == fast) {
            return true; // Cycle detected!
        }
    }
    return false; // Reached end of list -> No cycle
}
```

---

#### Problem 3.1: Linked List Cycle (LeetCode #141) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given `head`, the head of a linked list, determine if the linked list has a cycle in it. Return `true` if there is a cycle, or `false` otherwise.
* **Constraints**: Number of nodes is in range $[0, 10^4]$, $-10^5 \le \text{Node.val} \le 10^5$. Must use $O(1)$ memory.

##### 2. 🐢 Approach 1: Hash Set (Store Visited Node References)
* **Logic**: Traverse the list and insert each `ListNode` into a `HashSet`. If a node is already present, a cycle exists.
```java
// Time Complexity: O(N). Space Complexity: O(N) heap memory. Violates O(1) space constraint!
```

##### 3. ⚡ Approach 2: Optimal Floyd's Cycle-Finding Algorithm (Gold Standard)
```java
package com.leetcode.fastslow;

class ListNode {
    int val;
    ListNode next;
    ListNode(int x) { val = x; next = null; }
}

public class LinkedListCycle {
    public boolean hasCycle(ListNode head) {
        if (head == null || head.next == null) return false;

        ListNode slow = head;
        ListNode fast = head;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;

            if (slow == fast) return true; // Collision detected
        }
        return false;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) strictly constant memory.
```

##### 4. 📊 Comparative Trade-off Matrix
| Approach | Time Complexity | Space Complexity | Pros | Cons / Verdict |
| :--- | :--- | :--- | :--- | :--- |
| **1. Hash Set** | $O(N)$ | $O(N)$ | Simple to write | Allocates heap memory for every node |
| **2. Floyd's Algorithm**| $O(N)$ | $O(1)$ | Zero extra memory | Modifies no pointers |

---

#### Problem 3.2: Linked List Cycle II - Find Cycle Entry Node (LeetCode #142) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the head of a linked list, return the node where the cycle begins. If there is no cycle, return `null`. Do not modify the linked list.
* **Constraints**: Number of nodes in range $[0, 10^4]$. Space complexity must be $O(1)$.

##### 2. 👁️ Mathematical Proof Trace
```
Let L1 = distance from Head to Cycle Entrance.
Let L2 = distance from Cycle Entrance to Meeting Point.
Let C  = total circumference of Cycle.

Distance traveled by Slow = L1 + L2
Distance traveled by Fast = L1 + L2 + n*C (where n >= 1)
Since Fast runs at 2x speed of Slow:
2 * (L1 + L2) = L1 + L2 + n*C
=> L1 + L2 = n*C
=> L1 = n*C - L2 = (n - 1)*C + (C - L2)

INSIGHT: The distance from Head to Cycle Entrance (L1) EXACTLY EQUALS
the distance from the Meeting Point to the Cycle Entrance (C - L2)!
Action: Reset one pointer to Head. Advance BOTH pointers 1 step at a time until they collide!
```

##### 3. ⚡ Optimal Solution (Two-Phase Floyd's Algorithm)
```java
package com.leetcode.fastslow;

public class LinkedListCycleII {
    public ListNode detectCycle(ListNode head) {
        if (head == null || head.next == null) return null;

        ListNode slow = head;
        ListNode fast = head;
        boolean hasCycle = false;

        // Phase 1: Detect if a cycle exists
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                hasCycle = true;
                break;
            }
        }

        if (!hasCycle) return null;

        // Phase 2: Find cycle entrance node
        ListNode p1 = head;
        ListNode p2 = slow;

        while (p1 != p2) {
            p1 = p1.next;
            p2 = p2.next;
        }
        return p1; // Cycle entrance node!
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 3.3: Happy Number (LeetCode #202) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: A happy number is a number defined by the following process: Starting with any positive integer, replace the number by the sum of the squares of its digits. Repeat until the number equals 1 (where it stays), or it **loops endlessly in a cycle** which does not include 1. Return `true` if $n$ is happy.
* **Constraints**: $1 \le n \le 2^{31} - 1$.

##### 2. ⚡ Optimal Floyd's Cycle Detection on Implicit State Graph
```java
package com.leetcode.fastslow;

public class HappyNumber {
    public boolean isHappy(int n) {
        int slow = n;
        int fast = getNextSumOfSquares(n);

        // Treat sum transitions as a singly linked list: n -> next(n) -> next(next(n))
        while (fast != 1 && slow != fast) {
            slow = getNextSumOfSquares(slow);                   // 1 step
            fast = getNextSumOfSquares(getNextSumOfSquares(fast)); // 2 steps
        }
        return fast == 1;
    }

    private int getNextSumOfSquares(int n) {
        int totalSum = 0;
        while (n > 0) {
            int digit = n % 10;
            totalSum += digit * digit;
            n /= 10;
        }
        return totalSum;
    }
}
// Time Complexity: O(log N). Space Complexity: O(1) constant auxiliary space.
```

---

#### Problem 3.4: Find the Duplicate Number (LeetCode #287) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of integers `nums` containing $n + 1$ integers where each integer is in the range $[1, n]$ inclusive. There is only **one repeated number** in `nums`, return this duplicate number.
* **Constraints**: Must NOT modify the array `nums`, and must use only $O(1)$ extra space. $1 \le n \le 10^5$.

##### 2. 👁️ Implicit Linked List Mapping Trace
```
nums = [ 1,  3,  4,  2,  2 ]
Index:   0   1   2   3   4
Values act as pointers: i -> nums[i]
0 -> nums[0]=1 -> nums[1]=3 -> nums[3]=2 -> nums[2]=4 -> nums[4]=2 -> nums[2]=4 (CYCLE ON VALUE 2!)
Duplicate number = Entry point of the cycle!
```

##### 3. ⚡ Optimal Solution (Floyd's Tortoise and Hare on Array Pointers)
```java
package com.leetcode.fastslow;

public class FindDuplicateNumber {
    public int findDuplicate(int[] nums) {
        int slow = nums[0];
        int fast = nums[0];

        // Phase 1: Find intersection in the cycle
        do {
            slow = nums[slow];          // slow = slow.next
            fast = nums[nums[fast]];    // fast = fast.next.next
        } while (slow != fast);

        // Phase 2: Find cycle entrance (duplicate value)
        int p1 = nums[0];
        int p2 = slow;

        while (p1 != p2) {
            p1 = nums[p1];
            p2 = nums[p2];
        }
        return p1;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) without modifying input array.
```

---

#### Problem 3.5: Middle of the Linked List (LeetCode #876) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `head` of a singly linked list, return the middle node of the linked list. If there are two middle nodes, return the **second middle** node.
* **Constraints**: Number of nodes in range $[1, 100]$.

##### 2. ⚡ Optimal Single-Pass Solution
```java
package com.leetcode.fastslow;

public class MiddleLinkedList {
    public ListNode middleNode(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;

        // When fast reaches the end, slow is exactly at the middle node
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }
}
// Time Complexity: O(N) in a single pass. Space Complexity: O(1).
```

---

#### Problem 3.6: Palindrome Linked List (LeetCode #234) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `head` of a singly linked list, return `true` if it is a palindrome or `false` otherwise.
* **Constraints**: Number of nodes in range $[1, 10^5]$. Follow-up: Solve in $O(N)$ time and $O(1)$ space.

##### 2. ⚡ Optimal 3-Step Solution (Middle $\to$ Reverse Second Half $\to$ Compare)
```java
package com.leetcode.fastslow;

public class PalindromeLinkedList {
    public boolean isPalindrome(ListNode head) {
        if (head == null || head.next == null) return true;

        // 1. Find middle of list using Fast & Slow pointers
        ListNode slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        // 2. Reverse the second half of the linked list in-place
        ListNode secondHalfHead = reverseList(slow.next);

        // 3. Compare first half and reversed second half values
        ListNode p1 = head;
        ListNode p2 = secondHalfHead;
        boolean isPalin = true;

        while (p2 != null) {
            if (p1.val != p2.val) {
                isPalin = false;
                break;
            }
            p1 = p1.next;
            p2 = p2.next;
        }

        // (Optional) Restore the list by re-reversing
        slow.next = reverseList(secondHalfHead);

        return isPalin;
    }

    private ListNode reverseList(ListNode head) {
        ListNode prev = null, curr = head;
        while (curr != null) {
            ListNode nextTemp = curr.next;
            curr.next = prev;
            prev = curr;
            curr = nextTemp;
        }
        return prev;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) in-place.
```

---

#### Problem 3.7: Circular Array Loop (LeetCode #457) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: An array `nums` of non-zero integers represents a circular jump game. A jump from index $i$ moves `nums[i]` steps (forward if positive, backward if negative). Determine if there is a cycle of length $> 1$ where all moves in the cycle are in the **same direction**.
* **Constraints**: $1 \le \text{nums.length} \le 5000$, $-1000 \le \text{nums}[i] \le 1000$.

##### 2. ⚡ Optimal Fast/Slow Cycle Detection with Color Tagging
```java
package com.leetcode.fastslow;

public class CircularArrayLoop {
    public boolean circularArrayLoop(int[] nums) {
        int n = nums.length;

        for (int i = 0; i < n; i++) {
            if (nums[i] == 0) continue; // Already visited path

            int slow = i, fast = i;
            boolean isForward = nums[i] > 0;

            // Advance slow by 1 step and fast by 2 steps
            while (true) {
                slow = getNextIndex(nums, isForward, slow);
                if (slow == -1) break;

                fast = getNextIndex(nums, isForward, fast);
                if (fast == -1) break;
                fast = getNextIndex(nums, isForward, fast);
                if (fast == -1) break;

                if (slow == fast) {
                    // Check if cycle length > 1 (single-element loop is invalid)
                    if (slow == getNextIndex(nums, isForward, slow)) break;
                    return true;
                }
            }

            // Mark all nodes in this failed traversal path with 0 to prevent O(N^2) re-checks
            int curr = i;
            while (nums[curr] != 0 && (nums[curr] > 0) == isForward) {
                int next = ((curr + nums[curr]) % n + n) % n;
                nums[curr] = 0;
                curr = next;
            }
        }
        return false;
    }

    private int getNextIndex(int[] nums, boolean isForward, int current) {
        boolean direction = nums[current] > 0;
        if (direction != isForward) return -1; // Direction changed -> invalid loop

        int n = nums.length;
        int next = ((current + nums[current]) % n + n) % n;
        if (next == current) return -1; // 1-element self-loop is invalid

        return next;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 3.8: Reorder List (LeetCode #143) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a singly linked list $L_0 \to L_1 \to \dots \to L_{n-1} \to L_n$, reorder it to: $L_0 \to L_n \to L_1 \to L_{n-1} \to L_2 \to L_{n-2} \to \dots$. You may not modify the values in the list's nodes, only nodes themselves may be changed.
* **Constraints**: Number of nodes in range $[1, 5 \times 10^4]$.

##### 2. ⚡ Optimal Solution (Find Middle $\to$ Reverse 2nd Half $\to$ Interweave)
```java
package com.leetcode.fastslow;

public class ReorderList {
    public void reorderList(ListNode head) {
        if (head == null || head.next == null) return;

        // 1. Find the middle node
        ListNode slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        // 2. Reverse the second half of the list
        ListNode secondHalf = reverse(slow.next);
        slow.next = null; // Disconnect first half from second half

        // 3. Merge / Interweave the two halves
        ListNode firstHalf = head;
        while (secondHalf != null) {
            ListNode temp1 = firstHalf.next;
            ListNode temp2 = secondHalf.next;

            firstHalf.next = secondHalf;
            secondHalf.next = temp1;

            firstHalf = temp1;
            secondHalf = temp2;
        }
    }

    private ListNode reverse(ListNode head) {
        ListNode prev = null, curr = head;
        while (curr != null) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) in-place.
```

---

#### Problem 3.9: Remove Nth Node From End of List (LeetCode #19) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `head` of a linked list, remove the $n$-th node from the end of the list and return its head in a **single pass**.
* **Constraints**: Number of nodes in range $[1, 30]$, $1 \le n \le \text{size}$.

##### 2. ⚡ Optimal Two Pointers with Dummy Head
```java
package com.leetcode.fastslow;

public class RemoveNthFromEnd {
    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;

        ListNode fast = dummy;
        ListNode slow = dummy;

        // 1. Advance fast pointer by n + 1 steps
        for (int i = 0; i <= n; i++) {
            fast = fast.next;
        }

        // 2. Advance fast and slow together until fast reaches the end
        while (fast != null) {
            slow = slow.next;
            fast = fast.next;
        }

        // 3. slow is now right before the node to be deleted
        slow.next = slow.next.next;

        return dummy.next;
    }
}
// Time Complexity: O(N) single pass. Space Complexity: O(1).
```

---

#### Problem 3.10: Split Linked List in Parts (LeetCode #725) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the head of a singly linked list and an integer $k$, split the linked list into $k$ consecutive linked list parts. The length of each part should be as equal as possible: no two parts should have a size differing by more than one.
* **Constraints**: Number of nodes in $[0, 1000]$, $1 \le k \le 50$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.fastslow;

public class SplitLinkedListInParts {
    public ListNode[] splitListToParts(ListNode head, int k) {
        ListNode[] parts = new ListNode[k];

        // 1. Calculate total length of linked list
        int totalLength = 0;
        ListNode curr = head;
        while (curr != null) {
            totalLength++;
            curr = curr.next;
        }

        int baseSize = totalLength / k;
        int extraNodes = totalLength % k; // First `extraNodes` parts get baseSize + 1

        curr = head;
        for (int i = 0; i < k && curr != null; i++) {
            parts[i] = curr;
            int currentPartSize = baseSize + (i < extraNodes ? 1 : 0);

            // Traverse to the tail of the current part
            for (int j = 1; j < currentPartSize; j++) {
                curr = curr.next;
            }

            // Sever connection to the next part
            ListNode nextPartHead = curr.next;
            curr.next = null;
            curr = nextPartHead;
        }

        return parts;
    }
}
// Time Complexity: O(N + K). Space Complexity: O(1) auxiliary space (excluding result array).
```

### Pattern 4: Merge Intervals Pattern

```
========================= VISUAL MERGE INTERVALS BLUEPRINT =========================
Given intervals: [A.start, A.end] and [B.start, B.end] after sorting by start time:

Case 1: No Overlap (A and B are completely disjoint)
   A: [ ----- ]
   B:           [ ----- ]
   Action: Add A to output; Move to B.

Case 2: Partial Overlap (B overlaps with the end of A)
   A: [ --------- ]
   B:      [ --------- ]
   Merged: [ ----------- ] -> New Interval = [A.start, max(A.end, B.end)]

Case 3: Complete Subsumption (A completely envelops B)
   A: [ --------------- ]
   B:     [ ------ ]
   Merged: [ --------------- ] -> New Interval = [A.start, A.end]
===================================================================================
```

#### 🎯 Recognition Signals (When to use Merge Intervals):
* The problem gives a list of time intervals, meetings, segments, or coordinate ranges.
* You need to find **overlapping intervals, merge overlapping times, count concurrent rooms**, or find free gaps between schedules.
* **Golden Rule**: Almost always begins by **sorting intervals by start time** (`Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]))`).

#### 🛠️ Master Reusable Java Template:
```java
public int[][] mergeIntervalsTemplate(int[][] intervals) {
    if (intervals.length <= 1) return intervals;

    // 1. Sort intervals by start time in ascending order
    Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

    List<int[]> merged = new ArrayList<>();
    int[] current = intervals[0];
    merged.add(current);

    for (int[] next : intervals) {
        if (next[0] <= current[1]) { // Overlap detected!
            current[1] = Math.max(current[1], next[1]); // Merge intervals
        } else {
            current = next; // No overlap -> start a new interval
            merged.add(current);
        }
    }
    return merged.toArray(new int[merged.size()][]);
}
```

---

#### Problem 4.1: Merge Intervals (LeetCode #56) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of `intervals` where `intervals[i] = [start_i, end_i]`, merge all overlapping intervals, and return an array of the non-overlapping intervals that cover all the intervals in the input.
* **Constraints**: $1 \le \text{intervals.length} \le 10^4$, $\text{intervals}[i]\text{.length} == 2$, $0 \le \text{start}_i \le \text{end}_i \le 10^4$.

##### 2. 👁️ Visual Execution Trace
```
Intervals: [[1, 3], [8, 10], [2, 6], [15, 18]]
Step 1 (Sort by start): [[1, 3], [2, 6], [8, 10], [15, 18]]
Step 2: curr = [1, 3]
  - Compare with [2, 6]: 2 <= 3 (Overlap!) -> merge: curr[1] = max(3, 6) = 6. curr becomes [1, 6].
  - Compare with [8, 10]: 8 > 6 (No overlap) -> append [1, 6], curr becomes [8, 10].
  - Compare with [15, 18]: 15 > 10 (No overlap) -> append [8, 10], curr becomes [15, 18].
Output: [[1, 6], [8, 10], [15, 18]].
```

##### 3. ⚡ Optimal Solution (Sort + Single Scan)
```java
package com.leetcode.intervals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeIntervals {
    public int[][] merge(int[][] intervals) {
        if (intervals == null || intervals.length <= 1) return intervals;

        // Sort by start time (O(N log N))
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

        List<int[]> merged = new ArrayList<>();
        int[] currentInterval = intervals[0];
        merged.add(currentInterval);

        for (int i = 1; i < intervals.length; i++) {
            int[] nextInterval = intervals[i];

            if (nextInterval[0] <= currentInterval[1]) {
                // Overlapping: extend the end boundary
                currentInterval[1] = Math.max(currentInterval[1], nextInterval[1]);
            } else {
                // Disjoint: move currentInterval pointer forward
                currentInterval = nextInterval;
                merged.add(currentInterval);
            }
        }

        return merged.toArray(new int[merged.size()][]);
    }
}
// Time Complexity: O(N log N) due to sorting. Space Complexity: O(N) for result array.
```

---

#### Problem 4.2: Insert Interval (LeetCode #57) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an array of non-overlapping intervals `intervals` sorted by `start_i` in ascending order, and a `newInterval = [start, end]`. Insert `newInterval` into `intervals` such that `intervals` is still sorted and non-overlapping (merge if necessary).
* **Constraints**: $0 \le \text{intervals.length} \le 10^4$. Must achieve $O(N)$ linear time without re-sorting the whole list.

##### 2. ⚡ Optimal 3-Phase Single-Pass Solution
```java
package com.leetcode.intervals;

import java.util.ArrayList;
import java.util.List;

public class InsertInterval {
    public int[][] insert(int[][] intervals, int[] newInterval) {
        List<int[]> result = new ArrayList<>();
        int i = 0;
        int n = intervals.length;

        // Phase 1: Add all intervals that end BEFORE newInterval begins (no overlap)
        while (i < n && intervals[i][1] < newInterval[0]) {
            result.add(intervals[i]);
            i++;
        }

        // Phase 2: Merge all overlapping intervals with newInterval
        while (i < n && intervals[i][0] <= newInterval[1]) {
            newInterval[0] = Math.min(newInterval[0], intervals[i][0]);
            newInterval[1] = Math.max(newInterval[1], intervals[i][1]);
            i++;
        }
        result.add(newInterval); // Add merged composite interval

        // Phase 3: Add all remaining intervals that start AFTER newInterval ends
        while (i < n) {
            result.add(intervals[i]);
            i++;
        }

        return result.toArray(new int[result.size()][]);
    }
}
// Time Complexity: O(N) single linear pass. Space Complexity: O(N) for output list.
```

---

#### Problem 4.3: Non-overlapping Intervals (LeetCode #435) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of intervals `intervals` where `intervals[i] = [start_i, end_i]`, return the minimum number of intervals you need to remove to make the rest of the intervals non-overlapping.
* **Constraints**: $1 \le \text{intervals.length} \le 10^5$.

##### 2. ⚡ Optimal Greedy Interval Scheduling (Sort by End Time)
```java
package com.leetcode.intervals;

import java.util.Arrays;

public class NonOverlappingIntervals {
    public int eraseOverlapIntervals(int[][] intervals) {
        if (intervals.length == 0) return 0;

        // Greedy Key: Sort by END time to leave maximum room for future intervals!
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[1], b[1]));

        int removals = 0;
        int lastEnd = intervals[0][1];

        for (int i = 1; i < intervals.length; i++) {
            if (intervals[i][0] < lastEnd) {
                // Overlap detected! Remove current interval (which ends later than lastEnd)
                removals++;
            } else {
                // No overlap: update lastEnd to current interval's end
                lastEnd = intervals[i][1];
            }
        }
        return removals;
    }
}
// Time Complexity: O(N log N). Space Complexity: O(1) auxiliary.
```

---

#### Problem 4.4: Meeting Rooms (LeetCode #252) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of meeting time intervals `intervals` where `intervals[i] = [start_i, end_i]`, determine if a person could attend all meetings.
* **Constraints**: $0 \le \text{intervals.length} \le 10^4$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.intervals;

import java.util.Arrays;

public class MeetingRooms {
    public boolean canAttendMeetings(int[][] intervals) {
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

        for (int i = 0; i < intervals.length - 1; i++) {
            if (intervals[i][1] > intervals[i + 1][0]) {
                return false; // Meeting overlap conflict!
            }
        }
        return true;
    }
}
// Time Complexity: O(N log N). Space Complexity: O(1).
```

---

#### Problem 4.5: Meeting Rooms II (LeetCode #253) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of meeting time intervals `intervals` where `intervals[i] = [start_i, end_i]`, return the **minimum number of conference rooms required**.
* **Constraints**: $1 \le \text{intervals.length} \le 10^4$.

##### 2. 👁️ Visual Min-Heap Trace
```
Meetings: [[0, 30], [5, 10], [15, 20]]
Sorted by start: [[0, 30], [5, 10], [15, 20]]
Min-Heap stores END times of active meetings:
- Meeting 1 [0, 30]: Heap=[30] (1 room)
- Meeting 2 [5, 10]: Start 5 < Heap.peek(30) -> Room in use! Allocate new room. Heap=[10, 30] (2 rooms)
- Meeting 3 [15, 20]: Start 15 >= Heap.peek(10) -> Earliest meeting finished! Reuse room (poll 10, offer 20). Heap=[20, 30] (2 rooms).
Max rooms needed = 2.
```

##### 3. ⚡ Optimal Solution (Min-Heap Active Room Tracker)
```java
package com.leetcode.intervals;

import java.util.Arrays;
import java.util.PriorityQueue;

public class MeetingRoomsII {
    public int minMeetingRooms(int[][] intervals) {
        if (intervals == null || intervals.length == 0) return 0;

        // 1. Sort meetings by start time
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

        // 2. Min-heap tracks end times of currently occupied rooms
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();

        // Add the first meeting's end time
        minHeap.offer(intervals[0][1]);

        for (int i = 1; i < intervals.length; i++) {
            // If the earliest finishing meeting finishes BEFORE current meeting starts, reuse that room
            if (intervals[i][0] >= minHeap.peek()) {
                minHeap.poll();
            }

            // Assign room to current meeting (updates or adds end time)
            minHeap.offer(intervals[i][1]);
        }

        return minHeap.size();
    }
}
// Time Complexity: O(N log N). Space Complexity: O(N) for priority queue.
```

---

#### Problem 4.6: Interval List Intersections (LeetCode #986) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given two lists of closed intervals, `firstList` and `secondList`, where `firstList[i] = [start_i, end_i]` and `secondList[j] = [start_j, end_j]`. Each list of intervals is pairwise disjoint and in sorted order. Return the intersection of these two interval lists.
* **Constraints**: $0 \le \text{firstList.length}, \text{secondList.length} \le 1000$.

##### 2. ⚡ Optimal Two Pointers Intersection
```java
package com.leetcode.intervals;

import java.util.ArrayList;
import java.util.List;

public class IntervalIntersections {
    public int[][] intervalIntersection(int[][] firstList, int[][] secondList) {
        List<int[]> intersections = new ArrayList<>();
        int i = 0, j = 0;

        while (i < firstList.length && j < secondList.length) {
            // Intersection bounds: [max(startA, startB), min(endA, endB)]
            int start = Math.max(firstList[i][0], secondList[j][0]);
            int end = Math.min(firstList[i][1], secondList[j][1]);

            if (start <= end) {
                intersections.add(new int[]{start, end});
            }

            // Advance the pointer belonging to the interval that finishes earlier
            if (firstList[i][1] < secondList[j][1]) {
                i++;
            } else {
                j++;
            }
        }

        return intersections.toArray(new int[intersections.size()][]);
    }
}
// Time Complexity: O(N + M). Space Complexity: O(N + M) for output list.
```

---

#### Problem 4.7: Minimum Number of Arrows to Burst Balloons (LeetCode #452) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: There are spherical balloons taped to a flat wall that represents the XY-plane. The balloons are represented as a 2D integer array `points` where `points[i] = [x_start, x_end]`. An arrow shot at $x$ bursts all balloons whose $x_{\text{start}} \le x \le x_{\text{end}}$. Return the minimum number of arrows that must be shot to burst all balloons.
* **Constraints**: $1 \le \text{points.length} \le 10^5$, $-2^{31} \le x_{\text{start}} < x_{\text{end}} \le 2^{31} - 1$.

##### 2. ⚡ Optimal Greedy Solution (Sort by End Coordinate with Safe Integer Compare)
```java
package com.leetcode.intervals;

import java.util.Arrays;

public class MinimumArrowsBalloons {
    public int findMinArrowShots(int[][] points) {
        if (points.length == 0) return 0;

        // Use Integer.compare to avoid integer subtraction overflow on negative coordinates!
        Arrays.sort(points, (a, b) -> Integer.compare(a[1], b[1]));

        int arrows = 1;
        int currentArrowPos = points[0][1];

        for (int i = 1; i < points.length; i++) {
            // If current balloon starts AFTER arrow position, we need a new arrow
            if (points[i][0] > currentArrowPos) {
                arrows++;
                currentArrowPos = points[i][1];
            }
        }
        return arrows;
    }
}
// Time Complexity: O(N log N). Space Complexity: O(1).
```

---

#### Problem 4.8: Employee Free Time (LeetCode #759) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: We are given a list `schedule` of employees, which represents the working time for each employee. Return the list of finite intervals representing **common, positive-length free time** for all employees, sorted in order of start time.
* **Constraints**: $1 \le \text{schedule.length} \le 50$, total intervals $\le 10^4$.

##### 2. ⚡ Optimal Min-Heap $K$-Way Merge Solution
```java
package com.leetcode.intervals;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

class Interval {
    public int start, end;
    public Interval(int s, int e) { start = s; end = e; }
}

public class EmployeeFreeTime {
    public List<Interval> employeeFreeTime(List<List<Interval>> schedule) {
        List<Interval> freeTime = new ArrayList<>();
        // PriorityQueue stores flattened intervals sorted by start time
        PriorityQueue<Interval> minHeap = new PriorityQueue<>((a, b) -> Integer.compare(a.start, b.start));

        for (List<Interval> emp : schedule) {
            for (Interval iv : emp) {
                minHeap.offer(iv);
            }
        }

        Interval prev = minHeap.poll();
        while (!minHeap.isEmpty()) {
            Interval curr = minHeap.poll();

            if (prev.end < curr.start) {
                // Gap between working intervals is free time for all employees!
                freeTime.add(new Interval(prev.end, curr.start));
                prev = curr;
            } else {
                prev.end = Math.max(prev.end, curr.end);
            }
        }

        return freeTime;
    }
}
// Time Complexity: O(N log N) where N is total intervals. Space Complexity: O(N).
```

---

#### Problem 4.9: Video Stitching (LeetCode #1024) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given a series of video clips from a sporting event that lasted `time` seconds. These video clips can be overlapping with each other and have varying lengths. Return the minimum number of clips needed to stitch together a continuous video covering $[0, \text{time}]$. If impossible, return `-1`.
* **Constraints**: $1 \le \text{clips.length} \le 100$, $0 \le \text{time} \le 100$.

##### 2. ⚡ Optimal Greedy Jump Game on Intervals
```java
package com.leetcode.intervals;

public class VideoStitching {
    public int videoStitching(int[][] clips, int time) {
        // maxReach[i] stores the farthest point reachable starting at or before second i
        int[] maxReach = new int[time + 1];
        for (int[] clip : clips) {
            if (clip[0] <= time) {
                maxReach[clip[0]] = Math.max(maxReach[clip[0]], clip[1]);
            }
        }

        int clipsCount = 0;
        int currentEnd = 0, farthestNext = 0;

        for (int i = 0; i < time; i++) {
            farthestNext = Math.max(farthestNext, maxReach[i]);
            if (i == currentEnd) {
                if (farthestNext <= i) return -1; // Cannot progress forward
                clipsCount++;
                currentEnd = farthestNext;
            }
        }
        return clipsCount;
    }
}
// Time Complexity: O(N + Time). Space Complexity: O(Time).
```

---

#### Problem 4.10: My Calendar I (LeetCode #729) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Implement a `MyCalendar` class to store your events. A new event can be added if adding the event will not cause a **double booking** (an intersection of two events).
* **Constraints**: At most $1000$ calls will be made to `book(start, end)`.

##### 2. ⚡ Optimal Solution using Self-Balancing Binary Search Tree (`TreeMap`)
```java
package com.leetcode.intervals;

import java.util.TreeMap;

public class MyCalendar {
    private final TreeMap<Integer, Integer> calendar; // Start Time -> End Time

    public MyCalendar() {
        calendar = new TreeMap<>();
    }

    public boolean book(int start, int end) {
        // Find nearest event that starts AT OR BEFORE `start`
        Integer prevStart = calendar.floorKey(start);
        if (prevStart != null && calendar.get(prevStart) > start) {
            return false; // Overlap with previous meeting
        }

        // Find nearest event that starts AT OR AFTER `start`
        Integer nextStart = calendar.ceilingKey(start);
        if (nextStart != null && nextStart < end) {
            return false; // Overlap with next meeting
        }

        calendar.put(start, end);
        return true;
    }
}
// Time Complexity: O(log N) per booking. Space Complexity: O(N).
```

### Pattern 5: Cyclic Sort Pattern

```
========================= VISUAL CYCLIC SORT BLUEPRINT =========================
Array values are in range [1 ... N] (or [0 ... N]):
Every value `v` has a "True Home" index at `v - 1` (or `v`).

Initial Array:  [  3,   5,   2,   1,   4  ]  (Indices: 0, 1, 2, 3, 4)
i=0: val=3 -> True Home is index 2. Swap nums[0] with nums[2]:
                [  2,   5,   3,   1,   4  ]
i=0: val=2 -> True Home is index 1. Swap nums[0] with nums[1]:
                [  5,   2,   3,   1,   4  ]
i=0: val=5 -> True Home is index 4. Swap nums[0] with nums[4]:
                [  4,   2,   3,   1,   5  ]
i=0: val=4 -> True Home is index 3. Swap nums[0] with nums[3]:
                [  1,   2,   3,   4,   5  ]
Now nums[0] == 1 (in correct place!). Advance to i=1.
Sorted in O(N) Time and O(1) Auxiliary Space!
================================================================================
```

#### 🎯 Recognition Signals (When to use Cyclic Sort):
* Problem statements mentioning: **"Array containing numbers from 1 to N"** or **"0 to N"**.
* You need to find **missing numbers, duplicate numbers, the smallest missing positive integer**, or mismatching pairs in **$O(N)$ time and $O(1)$ space**.

#### 🛠️ Master Reusable Java Template:
```java
public void cyclicSortTemplate(int[] nums) {
    int i = 0;
    while (i < nums.length) {
        int correctIndex = nums[i] - 1; // Assuming range [1 ... N]
        if (nums[i] > 0 && nums[i] <= nums.length && nums[i] != nums[correctIndex]) {
            // Swap element to its correct home index
            int temp = nums[i];
            nums[i] = nums[correctIndex];
            nums[correctIndex] = temp;
        } else {
            i++; // Move to next index once current index has correct element
        }
    }
}
```

---

#### Problem 5.1: Missing Number (LeetCode #268) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array `nums` containing $n$ distinct numbers in the range $[0, n]$, return the only number in the range that is missing from the array.
* **Constraints**: $n == \text{nums.length}$, $1 \le n \le 10^4$, $0 \le \text{nums}[i] \le n$. All numbers are unique.

##### 2. ⚡ Optimal Solution (Cyclic Sort / XOR Bit Manipulation)
```java
package com.leetcode.cyclicsort;

public class MissingNumber {
    // Approach A: Cyclic Sort (O(N) Time, O(1) Space)
    public int missingNumber(int[] nums) {
        int i = 0;
        int n = nums.length;

        while (i < n) {
            int correctIndex = nums[i];
            if (nums[i] < n && nums[i] != nums[correctIndex]) {
                int temp = nums[i];
                nums[i] = nums[correctIndex];
                nums[correctIndex] = temp;
            } else {
                i++;
            }
        }

        // Find the first index that doesn't match its value
        for (int j = 0; j < n; j++) {
            if (nums[j] != j) return j;
        }
        return n; // If 0 to n-1 are present, n is missing
    }

    // Approach B: Bit Manipulation XOR (O(N) Time, O(1) Space, No Overflow)
    public int missingNumber_XOR(int[] nums) {
        int xor = nums.length;
        for (int i = 0; i < nums.length; i++) {
            xor ^= i ^ nums[i];
        }
        return xor;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 5.2: Find All Numbers Disappeared in an Array (LeetCode #448) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array `nums` of $n$ integers where `nums[i]` is in the range $[1, n]$, return an array of all the integers in the range $[1, n]$ that do not appear in `nums`.
* **Constraints**: $n == \text{nums.length}$, $1 \le n \le 10^5$, $1 \le \text{nums}[i] \le n$. Must solve without extra space.

##### 2. ⚡ Optimal Cyclic Sort Solution
```java
package com.leetcode.cyclicsort;

import java.util.ArrayList;
import java.util.List;

public class DisappearedNumbers {
    public List<Integer> findDisappearedNumbers(int[] nums) {
        int i = 0;
        while (i < nums.length) {
            int correctIdx = nums[i] - 1;
            if (nums[i] != nums[correctIdx]) {
                int temp = nums[i];
                nums[i] = nums[correctIdx];
                nums[correctIdx] = temp;
            } else {
                i++;
            }
        }

        List<Integer> missing = new ArrayList<>();
        for (int j = 0; j < nums.length; j++) {
            if (nums[j] != j + 1) {
                missing.add(j + 1);
            }
        }
        return missing;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) auxiliary.
```

---

#### Problem 5.3: Find All Duplicates in an Array (LeetCode #442) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums` of length $n$ where all the integers of `nums` are in the range $[1, n]$ and each integer appears **once or twice**, return an array of all the integers that appears twice.
* **Constraints**: Must write an algorithm that runs in $O(N)$ time and uses only $O(1)$ extra space.

##### 2. ⚡ Optimal Solution (Negative Index Tagging)
```java
package com.leetcode.cyclicsort;

import java.util.ArrayList;
import java.util.List;

public class FindAllDuplicates {
    public List<Integer> findDuplicates(int[] nums) {
        List<Integer> duplicates = new ArrayList<>();

        for (int i = 0; i < nums.length; i++) {
            int targetIdx = Math.abs(nums[i]) - 1;

            // If the value at targetIdx is already negative, we have seen this number before!
            if (nums[targetIdx] < 0) {
                duplicates.add(Math.abs(nums[i]));
            } else {
                nums[targetIdx] = -nums[targetIdx]; // Mark as visited by negating
            }
        }

        return duplicates;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) in-place.
```

---

#### Problem 5.4: Set Mismatch (LeetCode #645) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You have a set of integers from $1$ to $n$. Due to an error, one number was duplicated and another was lost. Find the number that occurs twice and the number that is missing. Return `[duplicate, missing]`.
* **Constraints**: $2 \le \text{nums.length} \le 10^4$, $1 \le \text{nums}[i] \le 10^4$.

##### 2. ⚡ Optimal Cyclic Sort Solution
```java
package com.leetcode.cyclicsort;

public class SetMismatch {
    public int[] findErrorNums(int[] nums) {
        int i = 0;
        while (i < nums.length) {
            int correctIdx = nums[i] - 1;
            if (nums[i] != nums[correctIdx]) {
                int temp = nums[i];
                nums[i] = nums[correctIdx];
                nums[correctIdx] = temp;
            } else {
                i++;
            }
        }

        for (int j = 0; j < nums.length; j++) {
            if (nums[j] != j + 1) {
                return new int[]{nums[j], j + 1}; // [Duplicate, Missing]
            }
        }
        return new int[]{-1, -1};
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 5.5: First Missing Positive (LeetCode #41) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an unsorted integer array `nums`, return the smallest positive integer that is not present in `nums`. You must implement an algorithm that runs in **$O(N)$ time and uses $O(1)$ auxiliary space**.
* **Constraints**: $1 \le \text{nums.length} \le 10^5$, $-2^{31} \le \text{nums}[i] \le 2^{31} - 1$.

##### 2. 👁️ Visual Execution Trace
```
nums = [ 3, 4, -1, 1 ] (Length N = 4)
Target Range for smallest missing positive: [1, 2, 3, 4, 5] (Always in [1 ... N + 1]).
Cycle Sort: Place positive integers in range [1 ... N] into their home index (nums[i] - 1):
i=0: val=3 -> swap with nums[2]: [ -1, 4, 3, 1 ]
i=0: val=-1 (<1, out of bounds) -> skip i++ (i=1)
i=1: val=4 -> swap with nums[3]: [ -1, 1, 3, 4 ]
i=1: val=1 -> swap with nums[0]: [ 1, -1, 3, 4 ]
Sorted State: [ 1, -1, 3, 4 ]
Scan: Index 0 has 1 (Correct). Index 1 has -1 != 2 (Mismatch!). Smallest missing positive = 2!
```

##### 3. ⚡ Optimal Solution (Hard $O(N)$ Time & $O(1)$ Space)
```java
package com.leetcode.cyclicsort;

public class FirstMissingPositive {
    public int firstMissingPositive(int[] nums) {
        int n = nums.length;
        int i = 0;

        while (i < n) {
            int correctIdx = nums[i] - 1;
            // Only swap if element is in positive valid range [1 ... N]
            // and is NOT already in its correct location (prevents infinite swap loops)
            if (nums[i] > 0 && nums[i] <= n && nums[i] != nums[correctIdx]) {
                int temp = nums[i];
                nums[i] = nums[correctIdx];
                nums[correctIdx] = temp;
            } else {
                i++;
            }
        }

        // Find first slot where nums[j] != j + 1
        for (int j = 0; j < n; j++) {
            if (nums[j] != j + 1) {
                return j + 1;
            }
        }

        return n + 1; // If all [1 ... N] are present, the answer is N + 1
    }
}
// Time Complexity: O(N) - Each number is swapped at most once into its correct place. Space Complexity: O(1).
```

---

#### Problem 5.6: Couples Holding Hands (LeetCode #765) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: There are $2N$ people sitting in $2N$ seats. Couples are numbered $(2i, 2i+1)$. Return the minimum number of swaps so that every couple is sitting side by side.
* **Constraints**: $2N == \text{row.length}$, $2 \le \text{row.length} \le 60$, `row[i]` is a permutation of $[0 \dots 2N-1]$.

##### 2. ⚡ Optimal Cyclic Sort Swap Greedy Solution
```java
package com.leetcode.cyclicsort;

public class CouplesHoldingHands {
    public int minSwapsCouples(int[] row) {
        int swaps = 0;
        int[] pos = new int[row.length]; // Stores index of each person
        for (int i = 0; i < row.length; i++) {
            pos[row[i]] = i;
        }

        for (int i = 0; i < row.length; i += 2) {
            int firstPerson = row[i];
            int partner = firstPerson ^ 1; // 0^1=1, 1^1=0, 2^1=3, 3^1=2

            if (row[i + 1] != partner) {
                swaps++;
                int partnerCurrentIndex = pos[partner];

                // Swap person at i+1 with the partner at partnerCurrentIndex
                row[partnerCurrentIndex] = row[i + 1];
                pos[row[i + 1]] = partnerCurrentIndex;

                row[i + 1] = partner;
                pos[partner] = i + 1;
            }
        }
        return swaps;
    }
}
// Time Complexity: O(N). Space Complexity: O(N) position lookup table.
```

---

#### Problem 5.7: Array Nesting (LeetCode #565) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an integer array `nums` of length $n$ where `nums` is a permutation of the numbers in the range $[0, n - 1]$. A set $S[k] = \{nums[k], nums[nums[k]], \dots\}$ builds a cycle. Return the longest length of a set $S[k]$.
* **Constraints**: $1 \le \text{nums.length} \le 10^5$.

##### 2. ⚡ Optimal In-Place Visited Tagging
```java
package com.leetcode.cyclicsort;

public class ArrayNesting {
    public int arrayNesting(int[] nums) {
        int maxCycleLength = 0;

        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != -1) {
                int count = 0;
                int curr = i;

                while (nums[curr] != -1) {
                    int next = nums[curr];
                    nums[curr] = -1; // Mark node as visited in-place
                    curr = next;
                    count++;
                }

                maxCycleLength = Math.max(maxCycleLength, count);
            }
        }
        return maxCycleLength;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) in-place modification.
```

---

#### Problem 5.8: Kth Missing Positive Number (LeetCode #1539) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array `arr` of positive integers sorted in strictly ascending order, and an integer $k$. Return the $k$-th positive integer that is missing from this array.
* **Constraints**: $1 \le \text{arr.length} \le 1000$, $1 \le \text{arr}[i], k \le 1000$.

##### 2. ⚡ Optimal Binary Search / Direct Counting Solution
```java
package com.leetcode.cyclicsort;

public class KthMissingPositive {
    public int findKthPositive(int[] arr, int k) {
        int low = 0, high = arr.length - 1;

        // Number of missing positive numbers before index `mid` = arr[mid] - (mid + 1)
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int missingBeforeMid = arr[mid] - (mid + 1);

            if (missingBeforeMid < k) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return low + k;
    }
}
// Time Complexity: O(log N). Space Complexity: O(1).
```

---

#### Problem 5.9: Split Array into Consecutive Subsequences (LeetCode #659) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums` sorted in non-decreasing order, determine if it is possible to split `nums` into one or more subsequences such that each subsequence consists of consecutive increasing integers and has length $\ge 3$.
* **Constraints**: $1 \le \text{nums.length} \le 10^4$.

##### 2. ⚡ Optimal Greedy Frequency & Chain Map Solution
```java
package com.leetcode.cyclicsort;

import java.util.HashMap;
import java.util.Map;

public class SplitConsecutiveSubsequences {
    public boolean isPossible(int[] nums) {
        Map<Integer, Integer> freq = new HashMap<>();
        Map<Integer, Integer> need = new HashMap<>(); // Number -> count of subsequences ending right before this number

        for (int num : nums) freq.put(num, freq.getOrDefault(num, 0) + 1);

        for (int num : nums) {
            if (freq.get(num) == 0) continue; // Already consumed in another sequence

            if (need.getOrDefault(num, 0) > 0) {
                // Option 1: Append to existing subsequence of length >= 3
                need.put(num, need.get(num) - 1);
                need.put(num + 1, need.getOrDefault(num + 1, 0) + 1);
            } else if (freq.getOrDefault(num + 1, 0) > 0 && freq.getOrDefault(num + 2, 0) > 0) {
                // Option 2: Form a new 3-element subsequence [num, num+1, num+2]
                freq.put(num + 1, freq.get(num + 1) - 1);
                freq.put(num + 2, freq.get(num + 2) - 1);
                need.put(num + 3, need.getOrDefault(num + 3, 0) + 1);
            } else {
                return false; // Cannot place num into any valid sequence
            }

            freq.put(num, freq.get(num) - 1);
        }

        return true;
    }
}
// Time Complexity: O(N). Space Complexity: O(N).
```

---

#### Problem 5.10: Find Duplicate in Array using Sign Negation (LeetCode #287 - Variant) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Solve the duplicate number identification in an array of size $N+1$ where elements are in $[1, N]$ by treating the array as its own direct hash table using sign negation.
* **Constraints**: $1 \le N \le 10^5$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.cyclicsort;

public class FindDuplicateSignNegation {
    public int findDuplicate(int[] nums) {
        for (int i = 0; i < nums.length; i++) {
            int index = Math.abs(nums[i]);
            if (nums[index] < 0) {
                return index; // Already visited!
            }
            nums[index] = -nums[index];
        }
        return -1;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

### Pattern 6: In-place Reversal of a Linked List Pattern

```
=================== VISUAL IN-PLACE LINKED LIST REVERSAL ===================
Initial:       null   [ 1 ] ---> [ 2 ] ---> [ 3 ] ---> [ 4 ] -> null
                 ^      ^
                prev   curr   (nextTemp = curr.next = [2])

Step 1:        null <- [ 1 ]     [ 2 ] ---> [ 3 ] ---> [ 4 ] -> null
                        ^          ^
                       prev       curr

Step 2:        null <- [ 1 ] <-- [ 2 ]      [ 3 ] ---> [ 4 ] -> null
                                   ^          ^
                                  prev       curr

Final:         null <- [ 1 ] <-- [ 2 ] <-- [ 3 ] <-- [ 4 ]
                                                       ^
                                                  New Head = prev
============================================================================
```

#### 🎯 Recognition Signals:
* Reversing all or a subsegment $[m \dots n]$ of a linked list **without allocating new nodes**.
* Reversing in blocks/groups of size $k$ (`Reverse Nodes in k-Group`).
* Solving list math or palindrome checks in **$O(1)$ memory**.

#### 🛠️ Master Reusable Java Template:
```java
public ListNode reverseLinkedListTemplate(ListNode head) {
    ListNode prev = null;
    ListNode curr = head;

    while (curr != null) {
        ListNode nextTemp = curr.next; // 1. Save next node reference
        curr.next = prev;              // 2. Reverse pointer to point backward
        prev = curr;                   // 3. Move prev pointer forward
        curr = nextTemp;               // 4. Move curr pointer forward
    }
    return prev; // prev is now the new head of the reversed list
}
```

---

#### Problem 6.1: Reverse Linked List (LeetCode #206) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `head` of a singly linked list, reverse the list, and return the reversed list.
* **Constraints**: Number of nodes in range $[0, 5000]$, $-5000 \le \text{Node.val} \le 5000$. Must solve both iteratively and recursively.

##### 2. ⚡ Optimal Solution (Iterative & Recursive Approaches)
```java
package com.leetcode.linkedlist;

public class ReverseLinkedList {
    // Approach A: Iterative (O(N) Time, O(1) Space)
    public ListNode reverseList_Iterative(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;

        while (curr != null) {
            ListNode nextTemp = curr.next;
            curr.next = prev;
            prev = curr;
            curr = nextTemp;
        }
        return prev;
    }

    // Approach B: Recursive (O(N) Time, O(N) Call Stack Space)
    public ListNode reverseList_Recursive(ListNode head) {
        if (head == null || head.next == null) return head;

        ListNode newHead = reverseList_Recursive(head.next);
        head.next.next = head; // Make next node point back to current node
        head.next = null;      // Sever forward link to prevent cycle

        return newHead;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) Iterative, O(N) Recursive.
```

---

#### Problem 6.2: Reverse Linked List II (Between positions left and right) (LeetCode #92) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `head` of a singly linked list and two integers `left` and `right` where $\text{left} \le \text{right}$, reverse the nodes of the list from position `left` to position `right`, and return the reversed list.
* **Constraints**: $1 \le n \le 500$, $1 \le \text{left} \le \text{right} \le n$. Must complete in a **single pass**.

##### 2. 👁️ Visual Sub-List Reversal Execution Trace
```
List: [ 1 ] -> [ 2 ] -> [ 3 ] -> [ 4 ] -> [ 5 ], left = 2, right = 4
prev = [1], curr = [2]
Pass 1: Move [3] after [1] -> [ 1 ] -> [ 3 ] -> [ 2 ] -> [ 4 ] -> [ 5 ]
Pass 2: Move [4] after [1] -> [ 1 ] -> [ 4 ] -> [ 3 ] -> [ 2 ] -> [ 5 ]
Result: [1] -> [4] -> [3] -> [2] -> [5].
```

##### 3. ⚡ Optimal Solution (Single-Pass Pointer Swapping)
```java
package com.leetcode.linkedlist;

public class ReverseLinkedListII {
    public ListNode reverseBetween(ListNode head, int left, int right) {
        if (head == null || left == right) return head;

        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode prev = dummy;

        // 1. Advance `prev` to node right before position `left`
        for (int i = 1; i < left; i++) {
            prev = prev.next;
        }

        ListNode curr = prev.next; // First node of subsegment to be reversed

        // 2. Perform in-place reversals
        for (int i = 0; i < right - left; i++) {
            ListNode temp = curr.next;
            curr.next = temp.next;
            temp.next = prev.next;
            prev.next = temp;
        }

        return dummy.next;
    }
}
// Time Complexity: O(N) single pass. Space Complexity: O(1).
```

---

#### Problem 6.3: Reverse Nodes in k-Group (LeetCode #25) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `head` of a linked list, reverse the nodes of the list $k$ at a time, and return the modified list. If the number of nodes is not a multiple of $k$ then left-out nodes, in the end, should remain as it is.
* **Constraints**: $1 \le k \le \text{length} \le 5000$. Memory must be $O(1)$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.linkedlist;

public class ReverseNodesInKGroup {
    public ListNode reverseKGroup(ListNode head, int k) {
        if (head == null || k <= 1) return head;

        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode prevGroupEnd = dummy;

        while (true) {
            // 1. Check if there are at least k nodes remaining
            ListNode kthNode = getKthNode(prevGroupEnd, k);
            if (kthNode == null) break; // Fewer than k nodes remain -> leave as is

            ListNode nextGroupStart = kthNode.next;
            ListNode curr = prevGroupEnd.next;
            ListNode prev = nextGroupStart;

            // 2. Reverse k nodes
            while (curr != nextGroupStart) {
                ListNode temp = curr.next;
                curr.next = prev;
                prev = curr;
                curr = temp;
            }

            // 3. Connect previous group to new reversed group head
            ListNode groupStart = prevGroupEnd.next;
            prevGroupEnd.next = kthNode;
            prevGroupEnd = groupStart;
        }

        return dummy.next;
    }

    private ListNode getKthNode(ListNode start, int k) {
        while (start != null && k > 0) {
            start = start.next;
            k--;
        }
        return start;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) in-place.
```

---

#### Problem 6.4: Rotate List (LeetCode #61) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `head` of a linked list, rotate the list to the right by $k$ places.
* **Constraints**: Number of nodes in range $[0, 500]$, $0 \le k \le 2 \times 10^9$.

##### 2. ⚡ Optimal Solution (Make Ring $\to$ Break at $(N - k \pmod N)$-th Node)
```java
package com.leetcode.linkedlist;

public class RotateList {
    public ListNode rotateRight(ListNode head, int k) {
        if (head == null || head.next == null || k == 0) return head;

        // 1. Compute list length and locate tail
        int length = 1;
        ListNode tail = head;
        while (tail.next != null) {
            tail = tail.next;
            length++;
        }

        // 2. Connect tail to head to form a circular ring
        tail.next = head;

        // 3. Find the new tail at (length - k % length) and new head
        k = k % length;
        int stepsToNewTail = length - k;
        ListNode newTail = tail;

        while (stepsToNewTail > 0) {
            newTail = newTail.next;
            stepsToNewTail--;
        }

        ListNode newHead = newTail.next;
        newTail.next = null; // Break circular ring

        return newHead;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 6.5: Swap Nodes in Pairs (LeetCode #24) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a linked list, swap every two adjacent nodes and return its head. You must solve the problem without modifying the values in the list's nodes (i.e., only nodes themselves may be changed).
* **Constraints**: Number of nodes in $[0, 100]$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.linkedlist;

public class SwapNodesInPairs {
    public ListNode swapPairs(ListNode head) {
        if (head == null || head.next == null) return head;

        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode prev = dummy;

        while (prev.next != null && prev.next.next != null) {
            ListNode first = prev.next;
            ListNode second = prev.next.next;

            // Swap pointers
            first.next = second.next;
            second.next = first;
            prev.next = second;

            // Advance prev pointer for next pair
            prev = first;
        }

        return dummy.next;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 6.6: Odd Even Linked List (LeetCode #328) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `head` of a singly linked list, group all the nodes with odd indices together followed by the nodes with even indices, and return the reordered list.
* **Constraints**: Number of nodes in range $[0, 10^4]$. Must solve in $O(N)$ time and $O(1)$ space.

##### 2. ⚡ Optimal Two-Pointer List Splitting Solution
```java
package com.leetcode.linkedlist;

public class OddEvenLinkedList {
    public ListNode oddEvenList(ListNode head) {
        if (head == null || head.next == null) return head;

        ListNode odd = head;
        ListNode even = head.next;
        ListNode evenHead = even; // Store head of even list to splice later

        while (even != null && even.next != null) {
            odd.next = even.next;
            odd = odd.next;

            even.next = odd.next;
            even = even.next;
        }

        odd.next = evenHead; // Splice even list to end of odd list
        return head;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 6.7: Reverse Nodes in Even Length Groups (LeetCode #2074) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given the head of a linked list. The nodes in the list are sequentially assigned to non-empty groups whose lengths form the sequence $1, 2, 3, 4, \dots$. Reverse the nodes in each group with an **even length**.
* **Constraints**: Number of nodes in range $[1, 10^5]$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.linkedlist;

public class ReverseEvenLengthGroups {
    public ListNode reverseEvenLengthGroups(ListNode head) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode prevGroupEnd = dummy;
        int groupSize = 1;

        while (prevGroupEnd.next != null) {
            // Count actual length of current group
            int actualLen = 0;
            ListNode curr = prevGroupEnd.next;
            while (curr != null && actualLen < groupSize) {
                curr = curr.next;
                actualLen++;
            }

            if (actualLen % 2 == 0) { // Even length -> reverse group
                ListNode groupStart = prevGroupEnd.next;
                ListNode revCurr = groupStart;
                ListNode revPrev = curr; // Points to start of next group

                for (int i = 0; i < actualLen; i++) {
                    ListNode nextTemp = revCurr.next;
                    revCurr.next = revPrev;
                    revPrev = revCurr;
                    revCurr = nextTemp;
                }

                prevGroupEnd.next = revPrev;
                prevGroupEnd = groupStart;
            } else { // Odd length -> skip reversal
                for (int i = 0; i < actualLen; i++) {
                    prevGroupEnd = prevGroupEnd.next;
                }
            }

            groupSize++;
        }

        return dummy.next;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 6.8: Double a Number Represented as a Linked List (LeetCode #2816) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given the `head` of a non-empty linked list representing a non-negative integer without leading zeroes. Return the `head` of the linked list after doubling it.
* **Constraints**: Number of nodes in $[1, 10^4]$, $\text{Node.val} \in [0, 9]$.

##### 2. ⚡ Optimal Single-Pass Solution (Lookahead Carry Injection)
```java
package com.leetcode.linkedlist;

public class DoubleLinkedListNumber {
    public ListNode doubleIt(ListNode head) {
        // If head value >= 5, doubling creates a carry that adds a new leading 1
        if (head.val >= 5) {
            ListNode newHead = new ListNode(0);
            newHead.next = head;
            head = newHead;
        }

        ListNode curr = head;
        while (curr != null) {
            curr.val = (curr.val * 2) % 10;
            // Check if next node will produce a carry (next.val >= 5)
            if (curr.next != null && curr.next.val >= 5) {
                curr.val += 1;
            }
            curr = curr.next;
        }

        return head;
    }
}
// Time Complexity: O(N) single forward pass. Space Complexity: O(1).
```

---

#### Problem 6.9: Add Two Numbers (LeetCode #2) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given two non-empty linked lists representing two non-negative integers. The digits are stored in **reverse order**, and each of their nodes contains a single digit. Add the two numbers and return the sum as a linked list.
* **Constraints**: Number of nodes in each linked list is in range $[1, 100]$, $0 \le \text{Node.val} \le 9$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.linkedlist;

public class AddTwoNumbers {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode dummy = new ListNode(0);
        ListNode curr = dummy;
        int carry = 0;

        while (l1 != null || l2 != null || carry != 0) {
            int val1 = (l1 != null) ? l1.val : 0;
            int val2 = (l2 != null) ? l2.val : 0;

            int sum = val1 + val2 + carry;
            carry = sum / 10;
            curr.next = new ListNode(sum % 10);
            curr = curr.next;

            if (l1 != null) l1 = l1.next;
            if (l2 != null) l2 = l2.next;
        }

        return dummy.next;
    }
}
// Time Complexity: O(max(N, M)). Space Complexity: O(max(N, M)) for result list.
```

---

#### Problem 6.10: Partition List (LeetCode #86) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `head` of a linked list and a value $x$, partition it such that all nodes **less than $x$** come before nodes greater than or equal to $x$, preserving original relative order.
* **Constraints**: Number of nodes in $[0, 200]$, $-100 \le \text{Node.val}, x \le 100$.

##### 2. ⚡ Optimal Solution (Two Dummy Lists)
```java
package com.leetcode.linkedlist;

public class PartitionList {
    public ListNode partition(ListNode head, int x) {
        ListNode lessHead = new ListNode(0);
        ListNode greaterHead = new ListNode(0);

        ListNode less = lessHead;
        ListNode greater = greaterHead;

        while (head != null) {
            if (head.val < x) {
                less.next = head;
                less = less.next;
            } else {
                greater.next = head;
                greater = greater.next;
            }
            head = head.next;
        }

        greater.next = null; // Important: terminate greater list to prevent cycle
        less.next = greaterHead.next; // Concatenate less list to greater list

        return lessHead.next;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) auxiliary space.
```

---

### Pattern 7: Tree Breadth-First Search (BFS / Level Order)

```
====================== VISUAL TREE BFS LEVEL-ORDER QUEUE ======================
              [ 1 ]               <--- Level 0 (Queue: [1], levelSize = 1)
             /     \
          [ 2 ]   [ 3 ]           <--- Level 1 (Queue: [2, 3], levelSize = 2)
         /   \     /   \
       [ 4 ] [ 5 ] [ 6 ] [ 7 ]    <--- Level 2 (Queue: [4, 5, 6, 7], levelSize = 4)

CRITICAL INVARIANT:
Snapshot `int levelSize = queue.size()` at the START of each while loop.
Iterate exactly `levelSize` times to process all nodes belonging strictly to the current horizontal level!
================================================================================
```

#### 🎯 Recognition Signals:
* Traversal required **level by level, horizontally**, from top to bottom.
* Finding the **shortest path in an unweighted tree/graph** or minimum moves/depth.
* Connecting nodes to their **horizontal neighbor** (`next` pointers) or printing level averages / right side view.

#### 🛠️ Master Reusable Java Template:
```java
public List<List<Integer>> levelOrderTemplate(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;

    Queue<TreeNode> queue = new ArrayDeque<>();
    queue.offer(root);

    while (!queue.isEmpty()) {
        int levelSize = queue.size(); // Snapshot current level width
        List<Integer> currentLevel = new ArrayList<>(levelSize);

        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();
            currentLevel.add(node.val);

            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        result.add(currentLevel);
    }
    return result;
}
```

---

#### Problem 7.1: Binary Tree Level Order Traversal (LeetCode #102) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `root` of a binary tree, return the level order traversal of its nodes' values (i.e., from left to right, level by level).
* **Constraints**: Number of nodes in $[0, 2000]$, $-1000 \le \text{Node.val} \le 1000$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.treebfs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int x) { val = x; }
}

public class LevelOrderTraversal {
    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<Integer> level = new ArrayList<>(levelSize);

            for (int i = 0; i < levelSize; i++) {
                TreeNode curr = queue.poll();
                level.add(curr.val);

                if (curr.left != null) queue.offer(curr.left);
                if (curr.right != null) queue.offer(curr.right);
            }
            result.add(level);
        }

        return result;
    }
}
// Time Complexity: O(N) visits every node once. Space Complexity: O(N) max queue width (up to N/2 nodes).
```

---

#### Problem 7.2: Binary Tree Zigzag Level Order Traversal (LeetCode #103) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `root` of a binary tree, return the zigzag level order traversal (i.e., from left to right, then right to left for the next level and alternate).
* **Constraints**: Number of nodes in $[0, 2000]$.

##### 2. ⚡ Optimal Solution (Queue + LinkedList `addFirst` / `addLast`)
```java
package com.leetcode.treebfs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ZigzagLevelOrder {
    public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);
        boolean leftToRight = true;

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            LinkedList<Integer> level = new LinkedList<>();

            for (int i = 0; i < levelSize; i++) {
                TreeNode curr = queue.poll();

                if (leftToRight) {
                    level.addLast(curr.val);
                } else {
                    level.addFirst(curr.val); // Prepend to reverse ordering in O(1)
                }

                if (curr.left != null) queue.offer(curr.left);
                if (curr.right != null) queue.offer(curr.right);
            }

            result.add(level);
            leftToRight = !leftToRight; // Flip direction for next level
        }

        return result;
    }
}
// Time Complexity: O(N). Space Complexity: O(N).
```

---

#### Problem 7.3: Binary Tree Right Side View (LeetCode #199) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `root` of a binary tree, imagine yourself standing on the **right side** of it, return the values of the nodes you can see ordered from top to bottom.
* **Constraints**: Number of nodes in $[0, 100]$.

##### 2. ⚡ Optimal Solution (Level-Order BFS Last Element Extraction)
```java
package com.leetcode.treebfs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class RightSideView {
    public List<Integer> rightSideView(TreeNode root) {
        List<Integer> rightView = new ArrayList<>();
        if (root == null) return rightView;

        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();

            for (int i = 0; i < levelSize; i++) {
                TreeNode curr = queue.poll();

                // If this is the LAST node in the current horizontal level, it is visible from the right!
                if (i == levelSize - 1) {
                    rightView.add(curr.val);
                }

                if (curr.left != null) queue.offer(curr.left);
                if (curr.right != null) queue.offer(curr.right);
            }
        }

        return rightView;
    }
}
// Time Complexity: O(N). Space Complexity: O(N).
```

---

#### Problem 7.4: Average of Levels in Binary Tree (LeetCode #637) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `root` of a binary tree, return the average value of the nodes on each level in the form of an array. Answers within $10^{-5}$ of the actual answer will be accepted.
* **Constraints**: Number of nodes in $[1, 10^4]$, $-2^{31} \le \text{Node.val} \le 2^{31} - 1$. Note: Sum may exceed 32-bit integer limits (use `double` / `long`).

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.treebfs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class AverageOfLevels {
    public List<Double> averageOfLevels(TreeNode root) {
        List<Double> averages = new ArrayList<>();
        if (root == null) return averages;

        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            double levelSum = 0.0;

            for (int i = 0; i < levelSize; i++) {
                TreeNode curr = queue.poll();
                levelSum += curr.val;

                if (curr.left != null) queue.offer(curr.left);
                if (curr.right != null) queue.offer(curr.right);
            }

            averages.add(levelSum / levelSize);
        }

        return averages;
    }
}
// Time Complexity: O(N). Space Complexity: O(N).
```

---

#### Problem 7.5: Minimum Depth of Binary Tree (LeetCode #111) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a binary tree, find its minimum depth. The minimum depth is the number of nodes along the shortest path from the root node down to the nearest **leaf node**.
* **Constraints**: Number of nodes in $[0, 10^5]$. Why BFS beats DFS: BFS terminates the moment it encounters the **first leaf node** without traversing the whole tree!

##### 2. ⚡ Optimal Solution (Early-Exit BFS)
```java
package com.leetcode.treebfs;

import java.util.ArrayDeque;
import java.util.Queue;

public class MinimumDepthBinaryTree {
    public int minDepth(TreeNode root) {
        if (root == null) return 0;

        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);
        int depth = 1;

        while (!queue.isEmpty()) {
            int levelSize = queue.size();

            for (int i = 0; i < levelSize; i++) {
                TreeNode curr = queue.poll();

                // First leaf node encountered gives the absolute minimum depth!
                if (curr.left == null && curr.right == null) {
                    return depth;
                }

                if (curr.left != null) queue.offer(curr.left);
                if (curr.right != null) queue.offer(curr.right);
            }
            depth++;
        }

        return depth;
    }
}
// Time Complexity: O(N) worst case, but terminates much earlier than DFS on unbalanced trees. Space Complexity: O(N).
```

---

#### Problem 7.6: Populating Next Right Pointers in Each Node (LeetCode #116) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given a **perfect binary tree** where all leaves are on the same level. Populate each `next` pointer to point to its next right node. If there is no next right node, the `next` pointer should be set to `NULL`.
* **Constraints**: Must use constant extra space $O(1)$.

##### 2. ⚡ Optimal Solution ($O(1)$ Space using Parent's `next` Links)
```java
package com.leetcode.treebfs;

class Node {
    public int val;
    public Node left, right, next;
    public Node(int _val) { val = _val; }
}

public class PopulatingNextRightPointers {
    public Node connect(Node root) {
        if (root == null) return null;

        Node leftmost = root;

        // Traverse level by level using already established `next` pointers (zero queue allocation!)
        while (leftmost.left != null) {
            Node curr = leftmost;

            while (curr != null) {
                // Connection 1: Connect left child to right child
                curr.left.next = curr.right;

                // Connection 2: Connect right child to neighbor's left child
                if (curr.next != null) {
                    curr.right.next = curr.next.left;
                }

                curr = curr.next; // Move horizontally across current level
            }

            leftmost = leftmost.left; // Move down to next level
        }

        return root;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) strictly constant auxiliary space.
```

---

#### Problem 7.7: Maximum Width of Binary Tree (LeetCode #662) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `root` of a binary tree, return the **maximum width** of the given tree. The maximum width of a tree is the maximum width among all levels. Width is the length between the end-nodes (the leftmost and rightmost non-null nodes), where null nodes in between are counted.
* **Constraints**: Number of nodes in $[1, 3000]$. Use 64-bit index indexing to prevent integer overflow.

##### 2. ⚡ Optimal Solution (Full Binary Tree Heap Indexing: $2i$ and $2i+1$)
```java
package com.leetcode.treebfs;

import java.util.ArrayDeque;
import java.util.Queue;

class Pair {
    TreeNode node;
    long index;
    Pair(TreeNode n, long idx) { node = n; index = idx; }
}

public class MaximumWidthBinaryTree {
    public int widthOfBinaryTree(TreeNode root) {
        if (root == null) return 0;

        Queue<Pair> queue = new ArrayDeque<>();
        queue.offer(new Pair(root, 0L));
        long maxWidth = 0;

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            long minIndexAtLevel = queue.peek().index; // Normalize index to prevent overflow
            long firstIdx = 0, lastIdx = 0;

            for (int i = 0; i < levelSize; i++) {
                Pair curr = queue.poll();
                long normalizedIdx = curr.index - minIndexAtLevel;

                if (i == 0) firstIdx = normalizedIdx;
                if (i == levelSize - 1) lastIdx = normalizedIdx;

                if (curr.node.left != null) {
                    queue.offer(new Pair(curr.node.left, 2 * normalizedIdx + 1));
                }
                if (curr.node.right != null) {
                    queue.offer(new Pair(curr.node.right, 2 * normalizedIdx + 2));
                }
            }

            maxWidth = Math.max(maxWidth, lastIdx - firstIdx + 1);
        }

        return (int) maxWidth;
    }
}
// Time Complexity: O(N). Space Complexity: O(N).
```

---

#### Problem 7.8: Level Order Successor of a Node (Binary Tree BFS) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a binary tree and a node key, find the **level order successor** of the given node in the tree. The level order successor is the node that appears immediately after the given node in the level order traversal.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.treebfs;

import java.util.ArrayDeque;
import java.util.Queue;

public class LevelOrderSuccessor {
    public TreeNode findSuccessor(TreeNode root, int key) {
        if (root == null) return null;

        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            TreeNode curr = queue.poll();

            if (curr.left != null) queue.offer(curr.left);
            if (curr.right != null) queue.offer(curr.right);

            // If current node matches key, the NEXT node in the queue is its level order successor!
            if (curr.val == key) {
                return queue.poll();
            }
        }
        return null;
    }
}
// Time Complexity: O(N). Space Complexity: O(N).
```

---

#### Problem 7.9: Word Ladder (LeetCode #127) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: A transformation sequence from word `beginWord` to word `endWord` using a dictionary `wordList` is a sequence of words such that each adjacent pair differs by a single letter. Return the number of words in the **shortest transformation sequence**, or `0` if no such sequence exists.
* **Constraints**: $1 \le \text{beginWord.length} \le 10$, $1 \le \text{wordList.length} \le 5000$.

##### 2. ⚡ Optimal Solution (Bidirectional / Standard BFS on Word Graph)
```java
package com.leetcode.treebfs;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class WordLadder {
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        Set<String> wordSet = new HashSet<>(wordList);
        if (!wordSet.contains(endWord)) return 0;

        Queue<String> queue = new ArrayDeque<>();
        queue.offer(beginWord);
        int level = 1;

        while (!queue.isEmpty()) {
            int levelSize = queue.size();

            for (int i = 0; i < levelSize; i++) {
                String currWord = queue.poll();
                if (currWord.equals(endWord)) return level;

                char[] chars = currWord.toCharArray();
                // Mutate every character position from 'a' to 'z'
                for (int j = 0; j < chars.length; j++) {
                    char originalChar = chars[j];

                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == originalChar) continue;
                        chars[j] = c;
                        String transformedWord = new String(chars);

                        if (wordSet.contains(transformedWord)) {
                            queue.offer(transformedWord);
                            wordSet.remove(transformedWord); // Remove visited word to prevent cycles
                        }
                    }
                    chars[j] = originalChar; // Backtrack
                }
            }
            level++;
        }

        return 0;
    }
}
// Time Complexity: O(N * 26 * L^2) where L is word length, N is dictionary size. Space Complexity: O(N * L).
```

---

#### Problem 7.10: Snakes and Ladders (LeetCode #909) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an $n \times n$ integer matrix `board` representing a Snakes and Ladders board labeled 1 to $n^2$ in Boustrophedon style (alternating left-to-right and right-to-left). Return the least number of dice rolls required to reach square $n^2$.
* **Constraints**: $2 \le n \le 20$.

##### 2. ⚡ Optimal BFS Shortest Path Solution
```java
package com.leetcode.treebfs;

import java.util.ArrayDeque;
import java.util.Queue;

public class SnakesAndLadders {
    public int snakesAndLadders(int[][] board) {
        int n = board.length;
        int target = n * n;
        boolean[] visited = new boolean[target + 1];

        Queue<Integer> queue = new ArrayDeque<>();
        queue.offer(1);
        visited[1] = true;
        int moves = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();

            for (int i = 0; i < size; i++) {
                int curr = queue.poll();
                if (curr == target) return moves;

                // Roll dice from 1 to 6
                for (int dice = 1; dice <= 6; dice++) {
                    int nextSquare = curr + dice;
                    if (nextSquare > target) break;

                    int[] pos = getCoordinates(nextSquare, n);
                    int row = pos[0], col = pos[1];

                    int destination = (board[row][col] != -1) ? board[row][col] : nextSquare;

                    if (!visited[destination]) {
                        visited[destination] = true;
                        queue.offer(destination);
                    }
                }
            }
            moves++;
        }

        return -1;
    }

    private int[] getCoordinates(int square, int n) {
        int r = (square - 1) / n;
        int c = (square - 1) % n;
        int row = n - 1 - r;
        int col = (r % 2 == 0) ? c : (n - 1 - c);
        return new int[]{row, col};
    }
}
// Time Complexity: O(N^2). Space Complexity: O(N^2).
```

### Pattern 8: Tree Depth-First Search (DFS / Backtracking on Trees)

```
====================== VISUAL TREE DFS TRAVERSALS & CALL STACK ======================
              [ 1 ]
             /     \
          [ 2 ]   [ 3 ]
         /   \
       [ 4 ] [ 5 ]

1. Pre-Order Traversal (Root -> Left -> Right):  [ 1, 2, 4, 5, 3 ] (Copying / Serialization)
2. In-Order Traversal (Left -> Root -> Right):   [ 4, 2, 5, 1, 3 ] (BST Sorted Order)
3. Post-Order Traversal (Left -> Right -> Root): [ 4, 5, 2, 3, 1 ] (Bottom-Up Subtree Aggregation)

RECURSIVE CALL STACK INVARIANT (Backtracking on Paths):
Path state accumulates down the branch: currentPath.add(node.val)
MUST unwind state when popping stack: currentPath.remove(currentPath.size() - 1)
======================================================================================
```

#### 🎯 Recognition Signals:
* Finding **root-to-leaf paths, path sums, tree diameter, maximum path sums**.
* Checking if a binary tree satisfies **BST properties** or validating subtrees.
* Finding the **Lowest Common Ancestor (LCA)** or constructing trees from traversals.

#### 🛠️ Master Reusable Java Template:
```java
public void dfsTreeBacktrackingTemplate(TreeNode root, int targetSum, List<Integer> currentPath, List<List<Integer>> allPaths) {
    if (root == null) return;

    // 1. Choose: add current node to path
    currentPath.add(root.val);

    // 2. Base Condition: check if leaf node matches target
    if (root.left == null && root.right == null && root.val == targetSum) {
        allPaths.add(new ArrayList<>(currentPath)); // Snapshot valid path
    } else {
        // 3. Explore child branches
        dfsTreeBacktrackingTemplate(root.left, targetSum - root.val, currentPath, allPaths);
        dfsTreeBacktrackingTemplate(root.right, targetSum - root.val, currentPath, allPaths);
    }

    // 4. Un-choose: Backtrack by removing current node before returning to parent caller
    currentPath.remove(currentPath.size() - 1);
}
```

---

#### Problem 8.1: Path Sum (LeetCode #112) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `root` of a binary tree and an integer `targetSum`, return `true` if the tree has a **root-to-leaf path** such that adding up all the values along the path equals `targetSum`.
* **Constraints**: Number of nodes in $[0, 5000]$, $-1000 \le \text{Node.val}, \text{targetSum} \le 1000$.

##### 2. ⚡ Optimal DFS Recursive Solution
```java
package com.leetcode.treedfs;

public class PathSum {
    public boolean hasPathSum(TreeNode root, int targetSum) {
        if (root == null) return false;

        // Leaf node check: if remaining sum equals node value, path is found
        if (root.left == null && root.right == null) {
            return root.val == targetSum;
        }

        // Recursively check left and right subtrees with deducted target sum
        return hasPathSum(root.left, targetSum - root.val) ||
               hasPathSum(root.right, targetSum - root.val);
    }
}
// Time Complexity: O(N). Space Complexity: O(H) call stack where H is tree height (O(log N) balanced, O(N) skewed).
```

---

#### Problem 8.2: Path Sum II (LeetCode #113) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `root` of a binary tree and an integer `targetSum`, return all **root-to-leaf paths** where the sum of the node values equals `targetSum`.
* **Constraints**: Number of nodes in $[0, 5000]$.

##### 2. ⚡ Optimal DFS Backtracking Solution
```java
package com.leetcode.treedfs;

import java.util.ArrayList;
import java.util.List;

public class PathSumII {
    public List<List<Integer>> pathSum(TreeNode root, int targetSum) {
        List<List<Integer>> result = new ArrayList<>();
        List<Integer> currentPath = new ArrayList<>();
        dfs(root, targetSum, currentPath, result);
        return result;
    }

    private void dfs(TreeNode node, int targetSum, List<Integer> path, List<List<Integer>> result) {
        if (node == null) return;

        path.add(node.val);

        if (node.left == null && node.right == null && node.val == targetSum) {
            result.add(new ArrayList<>(path)); // Deep copy valid path
        } else {
            dfs(node.left, targetSum - node.val, path, result);
            dfs(node.right, targetSum - node.val, path, result);
        }

        path.remove(path.size() - 1); // Backtrack
    }
}
// Time Complexity: O(N). Space Complexity: O(H) recursion stack.
```

---

#### Problem 8.3: Path Sum III (LeetCode #437) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `root` of a binary tree and an integer `targetSum`, return the number of paths where the sum of the values in the path equals `targetSum`. The path does **not need to start at the root or end at a leaf**, but it must go downwards.
* **Constraints**: Number of nodes in $[0, 1000]$, $-10^9 \le \text{Node.val}, \text{targetSum} \le 10^9$. Watch out for 64-bit integer overflow!

##### 2. ⚡ Optimal Solution ($O(N)$ Prefix Sum Hash Map on Trees)
```java
package com.leetcode.treedfs;

import java.util.HashMap;
import java.util.Map;

public class PathSumIII {
    public int pathSum(TreeNode root, int targetSum) {
        Map<Long, Integer> prefixSumMap = new HashMap<>();
        prefixSumMap.put(0L, 1); // Base condition: 1 way to have prefix sum 0
        return (int) dfs(root, 0L, targetSum, prefixSumMap);
    }

    private int dfs(TreeNode node, long currentSum, int targetSum, Map<Long, Integer> map) {
        if (node == null) return 0;

        currentSum += node.val;
        // Count paths ending at current node: currentSum - previousPrefixSum = targetSum
        int count = map.getOrDefault(currentSum - targetSum, 0);

        map.put(currentSum, map.getOrDefault(currentSum, 0) + 1);

        count += dfs(node.left, currentSum, targetSum, map);
        count += dfs(node.right, currentSum, targetSum, map);

        // Backtrack: remove current prefix sum count before returning to parent
        map.put(currentSum, map.get(currentSum) - 1);

        return count;
    }
}
// Time Complexity: O(N) single pass. Space Complexity: O(H) prefix map. (Beats naive O(N^2) double recursion).
```

---

#### Problem 8.4: Diameter of Binary Tree (LeetCode #543) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `root` of a binary tree, return the length of the **diameter** of the tree. The diameter of a binary tree is the length of the longest path between any two nodes in a tree. This path may or may not pass through the root.
* **Constraints**: Number of nodes in $[1, 10^4]$.

##### 2. ⚡ Optimal Bottom-Up Post-Order DFS Solution
```java
package com.leetcode.treedfs;

public class DiameterBinaryTree {
    private int maxDiameter = 0;

    public int diameterOfBinaryTree(TreeNode root) {
        maxHeight(root);
        return maxDiameter;
    }

    private int maxHeight(TreeNode node) {
        if (node == null) return 0;

        int leftHeight = maxHeight(node.left);
        int rightHeight = maxHeight(node.right);

        // Longest path through current node = leftHeight + rightHeight
        maxDiameter = Math.max(maxDiameter, leftHeight + rightHeight);

        // Return height of current subtree to parent caller
        return 1 + Math.max(leftHeight, rightHeight);
    }
}
// Time Complexity: O(N) visits every node once. Space Complexity: O(H) stack depth.
```

---

#### Problem 8.5: Binary Tree Maximum Path Sum (LeetCode #124) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: A **path** in a binary tree is a sequence of nodes where each pair of adjacent nodes in the sequence has an edge connecting them. Return the **maximum path sum** of any non-empty path.
* **Constraints**: Number of nodes in $[1, 3 \times 10^4]$, $-1000 \le \text{Node.val} \le 1000$.

##### 2. ⚡ Optimal Solution (Post-Order Bottom-Up Aggregation with Negative Pruning)
```java
package com.leetcode.treedfs;

public class MaxPathSumBinaryTree {
    private int globalMax = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        maxGain(root);
        return globalMax;
    }

    private int maxGain(TreeNode node) {
        if (node == null) return 0;

        // If a child branch contributes a negative sum, ignore it (take Math.max(0, gain))
        int leftGain = Math.max(0, maxGain(node.left));
        int rightGain = Math.max(0, maxGain(node.right));

        // Path passing THROUGH current node (acting as the apex/highest point of path)
        int currentPathSum = node.val + leftGain + rightGain;
        globalMax = Math.max(globalMax, currentPathSum);

        // Return the single best extending branch to the parent caller
        return node.val + Math.max(leftGain, rightGain);
    }
}
// Time Complexity: O(N). Space Complexity: O(H).
```

---

#### Problem 8.6: Lowest Common Ancestor of a Binary Tree (LeetCode #236) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a binary tree, find the lowest common ancestor (LCA) of two given nodes `p` and `q`. The LCA is defined between two nodes `p` and `q` as the lowest node in $T$ that has both `p` and `q` as descendants.
* **Constraints**: Number of nodes in $[2, 10^5]$, all `Node.val` are unique, `p != q`, and both `p` and `q` exist in the tree.

##### 2. ⚡ Optimal Post-Order DFS Solution
```java
package com.leetcode.treedfs;

public class LowestCommonAncestor {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        // Base Case: if root is null, or matches either target node p or q
        if (root == null || root == p || root == q) {
            return root;
        }

        TreeNode left = lowestCommonAncestor(root.left, p, q);
        TreeNode right = lowestCommonAncestor(root.right, p, q);

        // If both left and right return non-null, p and q are in separate subtrees -> current root is LCA!
        if (left != null && right != null) {
            return root;
        }

        // Otherwise return whichever subtree found one of the targets
        return (left != null) ? left : right;
    }
}
// Time Complexity: O(N). Space Complexity: O(H).
```

---

#### Problem 8.7: Validate Binary Search Tree (LeetCode #98) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `root` of a binary tree, determine if it is a valid binary search tree (BST). A valid BST satisfies: Left subtree contains only nodes with values **strictly less than** the node's key; Right subtree contains only nodes with values **strictly greater than** the node's key.
* **Constraints**: Number of nodes in $[1, 10^4]$, $-2^{31} \le \text{Node.val} \le 2^{31} - 1$. Must use `Long` bounds to avoid 32-bit integer overflow!

##### 2. ⚡ Optimal Solution (Range Propagation Bounds)
```java
package com.leetcode.treedfs;

public class ValidateBST {
    public boolean isValidBST(TreeNode root) {
        return validate(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    private boolean validate(TreeNode node, long minBound, long maxBound) {
        if (node == null) return true;

        // Current node value must strictly respect its inherited bounds
        if (node.val <= minBound || node.val >= maxBound) {
            return false;
        }

        // Left child bounded by (minBound, node.val); Right child bounded by (node.val, maxBound)
        return validate(node.left, minBound, node.val) &&
               validate(node.right, node.val, maxBound);
    }
}
// Time Complexity: O(N). Space Complexity: O(H).
```

---

#### Problem 8.8: Serialize and Deserialize Binary Tree (LeetCode #297) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Design an algorithm to serialize a binary tree into a string and deserialize that string back into the original tree structure.
* **Constraints**: Number of nodes in $[0, 10^4]$.

##### 2. ⚡ Optimal Pre-Order DFS Solution
```java
package com.leetcode.treedfs;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class Codec {
    private static final String NULL_NODE = "X";
    private static final String DELIMITER = ",";

    // Encodes a tree to a single string using Pre-Order DFS
    public String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        buildString(root, sb);
        return sb.toString();
    }

    private void buildString(TreeNode node, StringBuilder sb) {
        if (node == null) {
            sb.append(NULL_NODE).append(DELIMITER);
        } else {
            sb.append(node.val).append(DELIMITER);
            buildString(node.left, sb);
            buildString(node.right, sb);
        }
    }

    // Decodes your encoded data to tree
    public TreeNode deserialize(String data) {
        Queue<String> nodes = new LinkedList<>(Arrays.asList(data.split(DELIMITER)));
        return buildTree(nodes);
    }

    private TreeNode buildTree(Queue<String> nodes) {
        String val = nodes.poll();
        if (val.equals(NULL_NODE)) return null;

        TreeNode node = new TreeNode(Integer.parseInt(val));
        node.left = buildTree(nodes);
        node.right = buildTree(nodes);
        return node;
    }
}
// Time Complexity: O(N) for both serialize and deserialize. Space Complexity: O(N).
```

---

#### Problem 8.9: Flatten Binary Tree to Linked List (LeetCode #114) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the `root` of a binary tree, flatten the tree into a "linked list" in-place using the tree's `right` pointers, matching Pre-Order traversal order. All `left` pointers must be set to `null`.
* **Constraints**: Number of nodes in $[0, 2000]$. Space must be $O(1)$.

##### 2. ⚡ Optimal Solution (Morris Traversal / Threaded Pre-Order)
```java
package com.leetcode.treedfs;

public class FlattenBinaryTree {
    public void flatten(TreeNode root) {
        TreeNode curr = root;

        while (curr != null) {
            if (curr.left != null) {
                // Find the rightmost node of the left subtree (in-order predecessor)
                TreeNode predecessor = curr.left;
                while (predecessor.right != null) {
                    predecessor = predecessor.right;
                }

                // Splice current right subtree to predecessor's right
                predecessor.right = curr.right;

                // Move left subtree to right, and set left to null
                curr.right = curr.left;
                curr.left = null;
            }
            curr = curr.right;
        }
    }
}
// Time Complexity: O(N). Space Complexity: O(1) in-place without recursion stack.
```

---

#### Problem 8.10: Construct Binary Tree from Preorder and Inorder Traversal (LeetCode #105) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given two integer arrays `preorder` and `inorder` where `preorder` is the preorder traversal of a binary tree and `inorder` is the inorder traversal of the same tree, construct and return the binary tree.
* **Constraints**: $1 \le \text{preorder.length} \le 3000$, `inorder.length == preorder.length`, all values are unique.

##### 2. ⚡ Optimal Divide-and-Conquer Solution with HashMap
```java
package com.leetcode.treedfs;

import java.util.HashMap;
import java.util.Map;

public class ConstructTreeFromPreInOrder {
    private int preorderIndex = 0;
    private Map<Integer, Integer> inorderIndexMap;

    public TreeNode buildTree(int[] preorder, int[] inorder) {
        inorderIndexMap = new HashMap<>();
        for (int i = 0; i < inorder.length; i++) {
            inorderIndexMap.put(inorder[i], i);
        }
        return build(preorder, 0, inorder.length - 1);
    }

    private TreeNode build(int[] preorder, int inStart, int inEnd) {
        if (inStart > inEnd) return null;

        int rootVal = preorder[preorderIndex++];
        TreeNode root = new TreeNode(rootVal);

        int rootIndexInInorder = inorderIndexMap.get(rootVal);

        // Build left and right subtrees recursively
        root.left = build(preorder, inStart, rootIndexInInorder - 1);
        root.right = build(preorder, rootIndexInInorder + 1, inEnd);

        return root;
    }
}
// Time Complexity: O(N) linear time via HashMap index lookup. Space Complexity: O(N) map + recursion stack.
```

### Pattern 9: Two Heaps Pattern (Dynamic Median & Priority Scheduling)

```
====================== VISUAL TWO-HEAPS ARCHITECTURE ======================
Stream of numbers divided into two balanced halves:

   LOWER HALF (Smaller Numbers)              UPPER HALF (Larger Numbers)
   ============================              ===========================
       MAX-HEAP (maxHeap)                        MIN-HEAP (minHeap)
           [ 3 ] <--- Max element of lower           [ 5 ] <--- Min element of upper
          /     \                                   /     \
        [ 1 ]  [ 2 ]                              [ 7 ]  [ 9 ]

INVARIANTS:
1. Balance invariant: maxHeap.size() == minHeap.size()  OR  maxHeap.size() == minHeap.size() + 1
2. Order invariant: maxHeap.peek() <= minHeap.peek() (All items in maxHeap <= minHeap)
3. Median Calculation:
   - If odd total elements:  Median = maxHeap.peek()
   - If even total elements: Median = (maxHeap.peek() + minHeap.peek()) / 2.0
============================================================================
```

#### 🎯 Recognition Signals:
* Finding the **median in a dynamic streaming dataset** in $O(1)$ query time and $O(\log N)$ insertion.
* Finding the median in a sliding window.
* Complex scheduling where choices depend on two dynamic criteria simultaneously (e.g., maximize profit while respecting capital constraint in LC #502).

#### 🛠️ Master Reusable Java Template:
```java
class MedianFinderTemplate {
    private PriorityQueue<Integer> maxHeap; // Lower half
    private PriorityQueue<Integer> minHeap; // Upper half

    public MedianFinderTemplate() {
        maxHeap = new PriorityQueue<>((a, b) -> Integer.compare(b, a));
        minHeap = new PriorityQueue<>();
    }

    public void addNum(int num) {
        if (maxHeap.isEmpty() || num <= maxHeap.peek()) {
            maxHeap.offer(num);
        } else {
            minHeap.offer(num);
        }

        // Rebalance sizes
        if (maxHeap.size() > minHeap.size() + 1) {
            minHeap.offer(maxHeap.poll());
        } else if (minHeap.size() > maxHeap.size()) {
            maxHeap.offer(minHeap.poll());
        }
    }

    public double findMedian() {
        if (maxHeap.size() == minHeap.size()) {
            return (maxHeap.peek() + minHeap.peek()) / 2.0;
        }
        return maxHeap.peek();
    }
}
```

---

#### Problem 9.1: Find Median from Data Stream (LeetCode #295) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: The median is the middle value in an ordered integer list. Implement the `MedianFinder` class supporting `addNum(int num)` and `findMedian()`.
* **Constraints**: $-10^5 \le \text{num} \le 10^5$, up to $5 \times 10^4$ calls to `addNum` and `findMedian`.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.twoheaps;

import java.util.PriorityQueue;

public class MedianFinder {
    private PriorityQueue<Integer> maxHeap; // Stores smaller half
    private PriorityQueue<Integer> minHeap; // Stores larger half

    public MedianFinder() {
        maxHeap = new PriorityQueue<>((a, b) -> Integer.compare(b, a));
        minHeap = new PriorityQueue<>();
    }

    public void addNum(int num) {
        // Step 1: Add to maxHeap first
        maxHeap.offer(num);

        // Step 2: Ensure all elements in maxHeap are <= minHeap
        minHeap.offer(maxHeap.poll());

        // Step 3: Maintain size invariant (maxHeap size >= minHeap size)
        if (maxHeap.size() < minHeap.size()) {
            maxHeap.offer(minHeap.poll());
        }
    }

    public double findMedian() {
        if (maxHeap.size() > minHeap.size()) {
            return (double) maxHeap.peek();
        }
        return (maxHeap.peek() + (long) minHeap.peek()) / 2.0;
    }
}
// Time Complexity: O(log N) for addNum, O(1) for findMedian. Space Complexity: O(N).
```

---

#### Problem 9.2: Sliding Window Median (LeetCode #480) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array `nums` and an integer $k$, there is a sliding window of size $k$ which moves from left to right. Return the median array for each window in the original array.
* **Constraints**: $1 \le k \le \text{nums.length} \le 10^5$, $-2^{31} \le \text{nums}[i] \le 2^{31} - 1$. Must handle 64-bit integer overflow!

##### 2. ⚡ Optimal Solution (Two TreeSets with Index Tracking)
```java
package com.leetcode.twoheaps;

import java.util.TreeSet;

public class SlidingWindowMedian {
    public double[] medianSlidingWindow(int[] nums, int k) {
        // Use TreeSets of array indices with custom comparator to handle duplicate values
        TreeSet<Integer> maxSet = new TreeSet<>((a, b) -> nums[a] != nums[b] ? Integer.compare(nums[a], nums[b]) : Integer.compare(a, b));
        TreeSet<Integer> minSet = new TreeSet<>((a, b) -> nums[a] != nums[b] ? Integer.compare(nums[a], nums[b]) : Integer.compare(a, b));

        double[] medians = new double[nums.length - k + 1];

        for (int i = 0; i < nums.length; i++) {
            // 1. Add new element
            maxSet.add(i);
            minSet.add(maxSet.pollLast());

            if (minSet.size() > maxSet.size()) {
                maxSet.add(minSet.pollFirst());
            }

            // 2. Window is fully formed
            if (i >= k - 1) {
                // Record median
                if (k % 2 == 1) {
                    medians[i - k + 1] = (double) nums[maxSet.last()];
                } else {
                    medians[i - k + 1] = ((double) nums[maxSet.last()] + (double) nums[minSet.first()]) / 2.0;
                }

                // 3. Remove element leaving the window
                int outIdx = i - k + 1;
                if (!maxSet.remove(outIdx)) {
                    minSet.remove(outIdx);
                }

                // Rebalance sets
                if (maxSet.size() < minSet.size()) {
                    maxSet.add(minSet.pollFirst());
                } else if (maxSet.size() > minSet.size() + 1) {
                    minSet.add(maxSet.pollLast());
                }
            }
        }

        return medians;
    }
}
// Time Complexity: O(N log K). Space Complexity: O(K).
```

---

#### Problem 9.3: IPO / Maximize Capital (LeetCode #502) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given $n$ projects where project $i$ has profit `profits[i]` and requires minimum capital `capital[i]`. Initially you have $w$ capital. When you finish a project, its pure profit is added to your total capital. Pick at most $k$ distinct projects to **maximize your final capital**.
* **Constraints**: $1 \le k, n \le 10^5$, $0 \le \text{profits}[i], w \le 10^9$.

##### 2. ⚡ Optimal Two-Heap Greedy Solution
```java
package com.leetcode.twoheaps;

import java.util.Arrays;
import java.util.PriorityQueue;

public class IPO {
    public int findMaximizedCapital(int k, int w, int[] profits, int[] capital) {
        int n = profits.length;
        int[][] projects = new int[n][2]; // [capital, profit]
        for (int i = 0; i < n; i++) {
            projects[i][0] = capital[i];
            projects[i][1] = profits[i];
        }

        // Sort projects ascending by required capital
        Arrays.sort(projects, (a, b) -> Integer.compare(a[0], b[0]));

        // Max-Heap to store available profits for affordable projects
        PriorityQueue<Integer> maxProfitHeap = new PriorityQueue<>((a, b) -> Integer.compare(b, a));

        int projectIdx = 0;

        for (int step = 0; step < k; step++) {
            // Push all projects that we can currently afford into maxProfitHeap
            while (projectIdx < n && projects[projectIdx][0] <= w) {
                maxProfitHeap.offer(projects[projectIdx][1]);
                projectIdx++;
            }

            // If no affordable projects are left, break early
            if (maxProfitHeap.isEmpty()) break;

            // Greedily pick the project that yields the maximum profit
            w += maxProfitHeap.poll();
        }

        return w;
    }
}
// Time Complexity: O(N log N + K log N). Space Complexity: O(N).
```

---

#### Problem 9.4: Find Right Interval (LeetCode #436) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an array of intervals `intervals` where `intervals[i] = [starti, endi]`. The **right interval** for an interval $i$ is an interval $j$ such that $\text{start}_j \ge \text{end}_i$ and $\text{start}_j$ is minimized. Return an array of right interval indices for each interval.
* **Constraints**: $1 \le \text{intervals.length} \le 2 \times 10^4$.

##### 2. ⚡ Optimal Solution (Two Priority Queues / Binary Search)
```java
package com.leetcode.twoheaps;

import java.util.PriorityQueue;

public class FindRightInterval {
    public int[] findRightInterval(int[][] intervals) {
        int n = intervals.length;
        int[] result = new int[n];

        // Max-Heap sorted by start time
        PriorityQueue<int[]> maxStartHeap = new PriorityQueue<>((a, b) -> Integer.compare(b[0], a[0]));
        // Max-Heap sorted by end time
        PriorityQueue<int[]> maxEndHeap = new PriorityQueue<>((a, b) -> Integer.compare(b[0], a[0]));

        for (int i = 0; i < n; i++) {
            maxStartHeap.offer(new int[]{intervals[i][0], i});
            maxEndHeap.offer(new int[]{intervals[i][1], i});
        }

        while (!maxEndHeap.isEmpty()) {
            int[] endInterval = maxEndHeap.poll();
            int endVal = endInterval[0];
            int endOriginalIdx = endInterval[1];

            result[endOriginalIdx] = -1; // Default if no right interval exists
            int[] candidate = null;

            while (!maxStartHeap.isEmpty() && maxStartHeap.peek()[0] >= endVal) {
                candidate = maxStartHeap.poll();
            }

            if (candidate != null) {
                result[endOriginalIdx] = candidate[1];
                maxStartHeap.offer(candidate); // Put back the best right interval candidate
            }
        }

        return result;
    }
}
// Time Complexity: O(N log N). Space Complexity: O(N).
```

---

#### Problem 9.5: Task Scheduler (LeetCode #621) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a characters array `tasks`, representing tasks a CPU must execute, and a cooling interval $n$. Return the **minimum number of CPU intervals** required to finish all tasks.
* **Constraints**: $1 \le \text{tasks.length} \le 10^4$, $0 \le n \le 100$.

##### 2. ⚡ Optimal Solution (Max-Heap + Cooldown Queue / Math Greedy)
```java
package com.leetcode.twoheaps;

import java.util.Arrays;

public class TaskScheduler {
    public int leastInterval(char[] tasks, int n) {
        int[] freq = new int[26];
        for (char c : tasks) freq[c - 'A']++;

        Arrays.sort(freq);
        int maxFreq = freq[25];
        int idleSlots = (maxFreq - 1) * n;

        for (int i = 24; i >= 0 && freq[i] > 0; i--) {
            idleSlots -= Math.min(maxFreq - 1, freq[i]);
        }

        idleSlots = Math.max(0, idleSlots);
        return tasks.length + idleSlots;
    }
}
// Time Complexity: O(N). Space Complexity: O(1) constant 26-char frequency array.
```

---

#### Problem 9.6: Reorganize String (LeetCode #767) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a string `s`, rearrange the characters of `s` so that any two adjacent characters are not the same. Return any possible rearrangement of `s` or `""` if not possible.
* **Constraints**: $1 \le \text{s.length} \le 500$.

##### 2. ⚡ Optimal Max-Heap Greedy Solution
```java
package com.leetcode.twoheaps;

import java.util.PriorityQueue;

public class ReorganizeString {
    public String reorganizeString(String s) {
        int[] count = new int[26];
        for (char c : s.toCharArray()) count[c - 'a']++;

        // Max-Heap ordered by character frequency
        PriorityQueue<int[]> maxHeap = new PriorityQueue<>((a, b) -> Integer.compare(b[1], a[1]));
        for (int i = 0; i < 26; i++) {
            if (count[i] > 0) {
                if (count[i] > (s.length() + 1) / 2) return ""; // Pigeonhole impossibility
                maxHeap.offer(new int[]{i, count[i]});
            }
        }

        StringBuilder sb = new StringBuilder();
        int[] prev = null;

        while (!maxHeap.isEmpty()) {
            int[] curr = maxHeap.poll();
            sb.append((char) ('a' + curr[0]));
            curr[1]--;

            if (prev != null && prev[1] > 0) {
                maxHeap.offer(prev); // Re-insert previously used character
            }

            prev = curr;
        }

        return sb.toString();
    }
}
// Time Complexity: O(N log 26) = O(N). Space Complexity: O(26) = O(1).
```

---

#### Problem 9.7: Minimum Cost to Hire K Workers (LeetCode #857) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: There are $n$ workers. You are given `quality` and `wage`. You want to hire exactly $k$ workers to form a paid group such that every worker is paid proportional to their quality ratio, and each receives at least their minimum wage. Return the least amount of money needed.
* **Constraints**: $1 \le k \le n \le 10^4$.

##### 2. ⚡ Optimal Solution (Sort by Wage/Quality Ratio + Max-Heap for Quality Sum)
```java
package com.leetcode.twoheaps;

import java.util.Arrays;
import java.util.PriorityQueue;

public class MinCostHireWorkers {
    public double mincostToHireWorkers(int[] quality, int[] wage, int k) {
        int n = quality.length;
        double[][] workers = new double[n][2]; // [wage/quality ratio, quality]

        for (int i = 0; i < n; i++) {
            workers[i][0] = (double) wage[i] / quality[i];
            workers[i][1] = quality[i];
        }

        // Sort ascending by wage-to-quality ratio
        Arrays.sort(workers, (a, b) -> Double.compare(a[0], b[0]));

        PriorityQueue<Integer> maxQualityHeap = new PriorityQueue<>((a, b) -> Integer.compare(b, a));
        int qualitySum = 0;
        double minCost = Double.MAX_VALUE;

        for (double[] worker : workers) {
            double ratio = worker[0];
            int q = (int) worker[1];

            qualitySum += q;
            maxQualityHeap.offer(q);

            if (maxQualityHeap.size() > k) {
                qualitySum -= maxQualityHeap.poll(); // Evict worker with largest quality
            }

            if (maxQualityHeap.size() == k) {
                minCost = Math.min(minCost, qualitySum * ratio);
            }
        }

        return minCost;
    }
}
// Time Complexity: O(N log N + N log K). Space Complexity: O(N + K).
```

---

#### Problem 9.8: Maximum Performance of a Team (LeetCode #1383) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given two integers $n$ and $k$ and two integer arrays `speed` and `efficiency`. Performance of a team is the sum of its engineers' speeds multiplied by the **minimum efficiency** among its engineers. Return the maximum performance modulo $10^9 + 7$.
* **Constraints**: $1 \le k \le n \le 10^5$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.twoheaps;

import java.util.Arrays;
import java.util.PriorityQueue;

public class MaxPerformanceTeam {
    public int maxPerformance(int n, int[] speed, int[] efficiency, int k) {
        int[][] engineers = new int[n][2];
        for (int i = 0; i < n; i++) {
            engineers[i][0] = efficiency[i];
            engineers[i][1] = speed[i];
        }

        // Sort descending by efficiency
        Arrays.sort(engineers, (a, b) -> Integer.compare(b[0], a[0]));

        PriorityQueue<Integer> minSpeedHeap = new PriorityQueue<>();
        long speedSum = 0;
        long maxPerformance = 0;

        for (int[] eng : engineers) {
            int eff = eng[0];
            int spd = eng[1];

            speedSum += spd;
            minSpeedHeap.offer(spd);

            if (minSpeedHeap.size() > k) {
                speedSum -= minSpeedHeap.poll();
            }

            maxPerformance = Math.max(maxPerformance, speedSum * eff);
        }

        return (int) (maxPerformance % 1_000_000_007);
    }
}
// Time Complexity: O(N log N + N log K). Space Complexity: O(N + K).
```

---

#### Problem 9.9: Process Tasks Using Servers (LeetCode #1882) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You have $n$ servers with weights and $m$ tasks with execution times. Servers with smaller weight (and index if tied) are assigned first. Return array `ans` where `ans[j]` is the index of server assigned to task $j$.
* **Constraints**: $1 \le n, m \le 2 \times 10^5$.

##### 2. ⚡ Optimal Two Priority Queues (Free Servers + Busy Servers) Solution
```java
package com.leetcode.twoheaps;

import java.util.PriorityQueue;

public class ProcessTasksServers {
    public int[] assignTasks(int[] servers, int[] tasks) {
        int n = servers.length;
        int m = tasks.length;
        int[] result = new int[m];

        // Free servers: sorted by (weight, serverIndex)
        PriorityQueue<int[]> freeServers = new PriorityQueue<>((a, b) -> {
            if (a[0] != b[0]) return Integer.compare(a[0], b[0]);
            return Integer.compare(a[1], b[1]);
        });

        // Busy servers: sorted by (availableTime, weight, serverIndex)
        PriorityQueue<int[]> busyServers = new PriorityQueue<>((a, b) -> {
            if (a[2] != b[2]) return Integer.compare(a[2], b[2]);
            if (a[0] != b[0]) return Integer.compare(a[0], b[0]);
            return Integer.compare(a[1], b[1]);
        });

        for (int i = 0; i < n; i++) {
            freeServers.offer(new int[]{servers[i], i, 0});
        }

        int currentTime = 0;

        for (int taskIdx = 0; taskIdx < m; taskIdx++) {
            currentTime = Math.max(currentTime, taskIdx);

            // Free up servers that completed before or at currentTime
            while (!busyServers.isEmpty() && busyServers.peek()[2] <= currentTime) {
                freeServers.offer(busyServers.poll());
            }

            // If no servers available, fast-forward time to when the first busy server frees up
            if (freeServers.isEmpty()) {
                currentTime = busyServers.peek()[2];
                while (!busyServers.isEmpty() && busyServers.peek()[2] <= currentTime) {
                    freeServers.offer(busyServers.poll());
                }
            }

            int[] server = freeServers.poll();
            result[taskIdx] = server[1];
            server[2] = currentTime + tasks[taskIdx];
            busyServers.offer(server);
        }

        return result;
    }
}
// Time Complexity: O(M log N + N log N). Space Complexity: O(N).
```

---

#### Problem 9.10: Single-Threaded CPU (LeetCode #1834) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given $n$ tasks labeled 0 to $n - 1$ where `tasks[i] = [enqueueTimei, processingTimei]`. CPU picks the task with the **shortest processing time** (smallest index if tied). Return the order in which the CPU will process the tasks.
* **Constraints**: $1 \le n \le 10^5$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.twoheaps;

import java.util.Arrays;
import java.util.PriorityQueue;

public class SingleThreadedCPU {
    public int[] getOrder(int[][] tasks) {
        int n = tasks.length;
        int[][] sortedTasks = new int[n][3]; // [enqueueTime, processingTime, originalIndex]

        for (int i = 0; i < n; i++) {
            sortedTasks[i][0] = tasks[i][0];
            sortedTasks[i][1] = tasks[i][1];
            sortedTasks[i][2] = i;
        }

        // Sort by enqueue time
        Arrays.sort(sortedTasks, (a, b) -> Integer.compare(a[0], b[0]));

        // Min-Heap ordered by (processingTime, originalIndex)
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> {
            if (a[1] != b[1]) return Integer.compare(a[1], b[1]);
            return Integer.compare(a[2], b[2]);
        });

        int[] order = new int[n];
        int orderIdx = 0, taskIdx = 0, currentTime = 0;

        while (taskIdx < n || !pq.isEmpty()) {
            // If CPU is idle, advance time to the enqueue time of the next available task
            if (pq.isEmpty() && currentTime < sortedTasks[taskIdx][0]) {
                currentTime = sortedTasks[taskIdx][0];
            }

            // Push all tasks that have arrived by currentTime
            while (taskIdx < n && sortedTasks[taskIdx][0] <= currentTime) {
                pq.offer(sortedTasks[taskIdx]);
                taskIdx++;
            }

            int[] curr = pq.poll();
            currentTime += curr[1];
            order[orderIdx++] = curr[2];
        }

        return order;
    }
}
// Time Complexity: O(N log N). Space Complexity: O(N).
```

### Pattern 10: Subsets, Permutations & Backtracking Pattern

```
====================== VISUAL BACKTRACKING DECISION TREE ======================
Array: [ 1, 2, 3 ]

                                  [ ]  (Root / Empty Subset)
                     /             |             \
            [ 1 ]                [ 2 ]          [ 3 ]
           /     \                 |
      [ 1, 2 ]   [ 1, 3 ]       [ 2, 3 ]
         |
     [ 1, 2, 3 ]

BACKTRACKING FRAMEWORK:
1. Choose:   state.add(candidate)
2. Explore:  backtrack(nextIndex, state)
3. Unchoose: state.remove(state.size() - 1)  <-- Restores state before exploring sibling branch
================================================================================
```

#### 🎯 Recognition Signals:
* Finding **all combinations, subsets, permutations, partitions, or placements** (e.g., N-Queens, Sudoku).
* Exhaustive search space $O(2^N)$ or $O(N!)$ where pruning invalid branches is crucial.

#### 🛠️ Master Reusable Java Templates:
```java
// Template 1: Subsets / Combinations (with index to avoid backward combinations)
public void backtrackSubsets(int start, int[] nums, List<Integer> curr, List<List<Integer>> result) {
    result.add(new ArrayList<>(curr)); // Snapshot current combination
    for (int i = start; i < nums.length; i++) {
        if (i > start && nums[i] == nums[i - 1]) continue; // Skip duplicate values at same level
        curr.add(nums[i]);
        backtrackSubsets(i + 1, nums, curr, result);
        curr.remove(curr.size() - 1); // Backtrack
    }
}

// Template 2: Permutations (with boolean[] visited array)
public void backtrackPermutations(int[] nums, boolean[] visited, List<Integer> curr, List<List<Integer>> result) {
    if (curr.size() == nums.length) {
        result.add(new ArrayList<>(curr));
        return;
    }
    for (int i = 0; i < nums.length; i++) {
        if (visited[i]) continue;
        if (i > 0 && nums[i] == nums[i - 1] && !visited[i - 1]) continue; // Duplicate pruning
        visited[i] = true;
        curr.add(nums[i]);
        backtrackPermutations(nums, visited, curr, result);
        curr.remove(curr.size() - 1); // Backtrack
        visited[i] = false;
    }
}
```

---

#### Problem 10.1: Subsets (LeetCode #78) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums` of unique elements, return all possible subsets (the power set). The solution set must not contain duplicate subsets.
* **Constraints**: $1 \le \text{nums.length} \le 10$, $-10 \le \text{nums}[i] \le 10$. All elements are unique.

##### 2. ⚡ Optimal Backtracking Solution
```java
package com.leetcode.backtracking;

import java.util.ArrayList;
import java.util.List;

public class Subsets {
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(0, nums, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int start, int[] nums, List<Integer> current, List<List<Integer>> result) {
        result.add(new ArrayList<>(current)); // Deep copy subset snapshot

        for (int i = start; i < nums.length; i++) {
            current.add(nums[i]);                  // 1. Choose
            backtrack(i + 1, nums, current, result); // 2. Explore
            current.remove(current.size() - 1);    // 3. Unchoose (Backtrack)
        }
    }
}
// Time Complexity: O(N * 2^N) - 2^N total subsets, each taking O(N) to copy. Space Complexity: O(N) recursion stack.
```

---

#### Problem 10.2: Subsets II (With Duplicates) (LeetCode #90) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums` that may contain duplicates, return all possible subsets. The solution set must not contain duplicate subsets.
* **Constraints**: $1 \le \text{nums.length} \le 10$, $-10 \le \text{nums}[i] \le 10$.

##### 2. ⚡ Optimal Solution (Sort + Duplicate Skipping)
```java
package com.leetcode.backtracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubsetsII {
    public List<List<Integer>> subsetsWithDup(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums); // Sort to group duplicates together
        backtrack(0, nums, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int start, int[] nums, List<Integer> current, List<List<Integer>> result) {
        result.add(new ArrayList<>(current));

        for (int i = start; i < nums.length; i++) {
            // Prune duplicate sibling branches at the same tree depth
            if (i > start && nums[i] == nums[i - 1]) continue;

            current.add(nums[i]);
            backtrack(i + 1, nums, current, result);
            current.remove(current.size() - 1);
        }
    }
}
// Time Complexity: O(N * 2^N). Space Complexity: O(N).
```

---

#### Problem 10.3: Permutations (LeetCode #46) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array `nums` of distinct integers, return all the possible permutations. You can return the answer in any order.
* **Constraints**: $1 \le \text{nums.length} \le 6$. All integers are distinct.

##### 2. ⚡ Optimal Backtracking Solution
```java
package com.leetcode.backtracking;

import java.util.ArrayList;
import java.util.List;

public class Permutations {
    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        boolean[] visited = new boolean[nums.length];
        backtrack(nums, visited, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int[] nums, boolean[] visited, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == nums.length) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (visited[i]) continue;

            visited[i] = true;
            current.add(nums[i]);

            backtrack(nums, visited, current, result);

            current.remove(current.size() - 1); // Backtrack
            visited[i] = false;
        }
    }
}
// Time Complexity: O(N * N!) - N! permutations, each taking O(N) to copy. Space Complexity: O(N).
```

---

#### Problem 10.4: Permutations II (With Duplicates) (LeetCode #47) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a collection of numbers, `nums`, that might contain duplicates, return all possible unique permutations in any order.
* **Constraints**: $1 \le \text{nums.length} \le 8$, $-10 \le \text{nums}[i] \le 10$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.backtracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermutationsII {
    public List<List<Integer>> permuteUnique(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums); // Group duplicate numbers
        boolean[] visited = new boolean[nums.length];
        backtrack(nums, visited, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int[] nums, boolean[] visited, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == nums.length) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (visited[i]) continue;

            // Duplicate condition: if previous identical number has NOT been visited,
            // visiting current one first produces a duplicate branch
            if (i > 0 && nums[i] == nums[i - 1] && !visited[i - 1]) continue;

            visited[i] = true;
            current.add(nums[i]);

            backtrack(nums, visited, current, result);

            current.remove(current.size() - 1);
            visited[i] = false;
        }
    }
}
// Time Complexity: O(N * N!). Space Complexity: O(N).
```

---

#### Problem 10.5: Combinations (LeetCode #77) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given two integers $n$ and $k$, return all possible combinations of $k$ numbers chosen from the range $[1, n]$.
* **Constraints**: $1 \le n \le 20$, $1 \le k \le n$.

##### 2. ⚡ Optimal Backtracking Solution with Pruning
```java
package com.leetcode.backtracking;

import java.util.ArrayList;
import java.util.List;

public class Combinations {
    public List<List<Integer>> combine(int n, int k) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(1, n, k, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int start, int n, int k, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }

        // Optimization Pruning: stop looping if remaining elements are not enough to reach size k
        int remainingNeeded = k - current.size();
        for (int i = start; i <= n - remainingNeeded + 1; i++) {
            current.add(i);
            backtrack(i + 1, n, k, current, result);
            current.remove(current.size() - 1);
        }
    }
}
// Time Complexity: O(k * C(n, k)). Space Complexity: O(k).
```

---

#### Problem 10.6: Combination Sum (LeetCode #39) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of distinct integers `candidates` and a target integer `target`, return a list of all unique combinations of candidates where the chosen numbers sum to target. The same number may be chosen an **unlimited number of times**.
* **Constraints**: $1 \le \text{candidates.length} \le 30$, $2 \le \text{candidates}[i] \le 40$, $1 \le \text{target} \le 40$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.backtracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombinationSum {
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(candidates);
        backtrack(0, candidates, target, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int start, int[] candidates, int remaining, List<Integer> current, List<List<Integer>> result) {
        if (remaining == 0) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < candidates.length; i++) {
            if (candidates[i] > remaining) break; // Sorted array allows early pruning break!

            current.add(candidates[i]);
            // Notice: we pass `i` (NOT `i + 1`) because elements can be reused unlimited times!
            backtrack(i, candidates, remaining - candidates[i], current, result);
            current.remove(current.size() - 1);
        }
    }
}
// Time Complexity: O(N^(T/M)) where T is target, M is min candidate value. Space Complexity: O(T/M).
```

---

#### Problem 10.7: Combination Sum II (LeetCode #40) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a collection of candidate numbers (`candidates`) and a target number (`target`), find all unique combinations where candidate numbers sum to `target`. Each number in candidates may only be used **once** in the combination.
* **Constraints**: $1 \le \text{candidates.length} \le 100$, $1 \le \text{target} \le 30$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.backtracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombinationSumII {
    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(candidates);
        backtrack(0, candidates, target, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int start, int[] candidates, int remaining, List<Integer> current, List<List<Integer>> result) {
        if (remaining == 0) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < candidates.length; i++) {
            if (candidates[i] > remaining) break; // Prune
            if (i > start && candidates[i] == candidates[i - 1]) continue; // Skip duplicate choices

            current.add(candidates[i]);
            backtrack(i + 1, candidates, remaining - candidates[i], current, result);
            current.remove(current.size() - 1);
        }
    }
}
// Time Complexity: O(2^N). Space Complexity: O(N).
```

---

#### Problem 10.8: Letter Combinations of a Phone Number (LeetCode #17) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a string containing digits from $2-9$ inclusive, return all possible letter combinations that the number could represent based on telephone buttons.
* **Constraints**: $0 \le \text{digits.length} \le 4$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.backtracking;

import java.util.ArrayList;
import java.util.List;

public class LetterCombinationsPhone {
    private static final String[] MAPPINGS = {
        "", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"
    };

    public List<String> letterCombinations(String digits) {
        List<String> result = new ArrayList<>();
        if (digits == null || digits.isEmpty()) return result;

        backtrack(0, digits, new StringBuilder(), result);
        return result;
    }

    private void backtrack(int index, String digits, StringBuilder current, List<String> result) {
        if (index == digits.length()) {
            result.add(current.toString());
            return;
        }

        String letters = MAPPINGS[digits.charAt(index) - '0'];
        for (char c : letters.toCharArray()) {
            current.append(c);
            backtrack(index + 1, digits, current, result);
            current.deleteCharAt(current.length() - 1); // Backtrack
        }
    }
}
// Time Complexity: O(4^N * N) where N is digits length. Space Complexity: O(N).
```

---

#### Problem 10.9: Generate Parentheses (LeetCode #22) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given $n$ pairs of parentheses, write a function to generate all combinations of well-formed parentheses.
* **Constraints**: $1 \le n \le 8$.

##### 2. ⚡ Optimal Solution (Catalan Number Backtracking with Valid Count Bounds)
```java
package com.leetcode.backtracking;

import java.util.ArrayList;
import java.util.List;

public class GenerateParentheses {
    public List<String> generateParenthesis(int n) {
        List<String> result = new ArrayList<>();
        backtrack(0, 0, n, new StringBuilder(), result);
        return result;
    }

    private void backtrack(int openCount, int closeCount, int max, StringBuilder sb, List<String> result) {
        if (sb.length() == max * 2) {
            result.add(sb.toString());
            return;
        }

        // We can add '(' if we haven't used all `max` open parentheses
        if (openCount < max) {
            sb.append('(');
            backtrack(openCount + 1, closeCount, max, sb, result);
            sb.deleteCharAt(sb.length() - 1);
        }

        // We can add ')' only if closed count < open count (prevents invalid prefixes)
        if (closeCount < openCount) {
            sb.append(')');
            backtrack(openCount, closeCount + 1, max, sb, result);
            sb.deleteCharAt(sb.length() - 1);
        }
    }
}
// Time Complexity: O(4^N / sqrt(N)) (N-th Catalan Number). Space Complexity: O(N).
```

---

#### Problem 10.10: N-Queens (LeetCode #51) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: The $n$-queens puzzle is the problem of placing $n$ queens on an $n \times n$ chessboard such that no two queens attack each other (no two queens share the same row, column, or diagonal). Return all distinct solutions.
* **Constraints**: $1 \le n \le 9$.

##### 2. ⚡ Optimal Solution ($O(1)$ Diagonal Bitset/Boolean Tracking)
```java
package com.leetcode.backtracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NQueens {
    public List<List<String>> solveNQueens(int n) {
        List<List<String>> result = new ArrayList<>();
        char[][] board = new char[n][n];
        for (char[] row : board) Arrays.fill(row, '.');

        boolean[] cols = new boolean[n];
        boolean[] diag1 = new boolean[2 * n]; // row - col + (n - 1)
        boolean[] diag2 = new boolean[2 * n]; // row + col

        backtrack(0, n, board, cols, diag1, diag2, result);
        return result;
    }

    private void backtrack(int row, int n, char[][] board, boolean[] cols, boolean[] diag1, boolean[] diag2, List<List<String>> result) {
        if (row == n) {
            List<String> validBoard = new ArrayList<>();
            for (char[] r : board) validBoard.add(new String(r));
            result.add(validBoard);
            return;
        }

        for (int col = 0; col < n; col++) {
            int d1 = row - col + (n - 1);
            int d2 = row + col;

            if (cols[col] || diag1[d1] || diag2[d2]) continue; // Attacked!

            // Place queen
            board[row][col] = 'Q';
            cols[col] = diag1[d1] = diag2[d2] = true;

            backtrack(row + 1, n, board, cols, diag1, diag2, result);

            // Backtrack
            board[row][col] = '.';
            cols[col] = diag1[d1] = diag2[d2] = false;
        }
    }
}
// Time Complexity: O(N!). Space Complexity: O(N^2) for board + O(N) for bitsets.
```

### Pattern 11: Modified Binary Search Pattern

```
========================= VISUAL BINARY SEARCH ON ANSWER SPACE =========================
Search Space:   [ Low .................................................... High ]
Feasibility:    [ False, False, False, False, True,  True,  True,  True,  True  ]
                                              ^
                               First Valid Answer (Target Minimization)

RULE 1: Calculate mid safely to prevent 32-bit overflow:
        int mid = low + (high - low) / 2;

RULE 2: Loop condition `low <= high` vs `low < high`:
        - Exact search: `low <= high`, update `low = mid + 1`, `high = mid - 1`
        - Boundary / Monotonic search: `low < high`, update `low = mid + 1`, `high = mid`
========================================================================================
```

#### 🎯 Recognition Signals:
* Sorted array or **rotated sorted array**.
* Searching in $O(\log N)$ time constraint.
* Optimization problems: **"Find the minimum capacity / speed / distance such that condition is satisfied"** (Binary Search on Answer Space).

#### 🛠️ Master Reusable Java Templates:
```java
// Template 1: Binary Search on Answer Space (Minimization)
public int binarySearchOnAnswer(int minVal, int maxVal) {
    int low = minVal, high = maxVal;
    while (low < high) {
        int mid = low + (high - low) / 2;
        if (isValid(mid)) {
            high = mid; // Try searching for smaller valid answer in lower half
        } else {
            low = mid + 1; // Answer must be larger
        }
    }
    return low;
}
```

---

#### Problem 11.1: Binary Search (LeetCode #704) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of integers `nums` which is sorted in ascending order, and an integer `target`, write a function to search `target` in `nums`. If `target` exists, then return its index. Otherwise, return `-1`.
* **Constraints**: $1 \le \text{nums.length} \le 10^4$, all integers in `nums` are unique.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.binarysearch;

public class BinarySearch {
    public int search(int[] nums, int target) {
        int low = 0, high = nums.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2; // Prevents overflow

            if (nums[mid] == target) {
                return mid;
            } else if (nums[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return -1;
    }
}
// Time Complexity: O(log N). Space Complexity: O(1).
```

---

#### Problem 11.2: Search in Rotated Sorted Array (LeetCode #33) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the array `nums` after the possible rotation and an integer `target`, return the index of `target` if it is in `nums`, or `-1` if it is not in `nums`. You must write an algorithm with $O(\log N)$ runtime complexity.
* **Constraints**: $1 \le \text{nums.length} \le 5000$, all values are unique.

##### 2. 👁️ Visual Sorted Half Identification
```
Array: [ 4, 5, 6, 7, 0, 1, 2 ], Target = 0
low = 0 (val 4), high = 6 (val 2), mid = 3 (val 7)
Is Left Half [4 ... 7] sorted? Yes (nums[low] <= nums[mid] -> 4 <= 7).
Is target 0 within [4 ... 7]? No (target < nums[low] -> 0 < 4).
-> Discard left half, search right half: low = mid + 1 = 4.
New subsegment: [ 0, 1, 2 ] -> Found target 0 at index 4!
```

##### 3. ⚡ Optimal Solution
```java
package com.leetcode.binarysearch;

public class SearchRotatedSortedArray {
    public int search(int[] nums, int target) {
        int low = 0, high = nums.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            if (nums[mid] == target) return mid;

            // Check if LEFT half is sorted
            if (nums[low] <= nums[mid]) {
                // Target is within the sorted left half
                if (target >= nums[low] && target < nums[mid]) {
                    high = mid - 1;
                } else {
                    low = mid + 1;
                }
            }
            // Otherwise, RIGHT half must be sorted
            else {
                // Target is within the sorted right half
                if (target > nums[mid] && target <= nums[high]) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
        }

        return -1;
    }
}
// Time Complexity: O(log N). Space Complexity: O(1).
```

---

#### Problem 11.3: Search in Rotated Sorted Array II (With Duplicates) (LeetCode #81) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the array `nums` after rotation that **may contain duplicate elements**, return `true` if `target` is in `nums`, or `false` otherwise.
* **Constraints**: $1 \le \text{nums.length} \le 5000$.

##### 2. ⚡ Optimal Solution (Handling `nums[low] == nums[mid] == nums[high]`)
```java
package com.leetcode.binarysearch;

public class SearchRotatedArrayII {
    public boolean search(int[] nums, int target) {
        int low = 0, high = nums.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            if (nums[mid] == target) return true;

            // Handle ambiguous duplicate case where we cannot determine which half is sorted
            if (nums[low] == nums[mid] && nums[mid] == nums[high]) {
                low++;
                high--;
                continue;
            }

            // Left half is sorted
            if (nums[low] <= nums[mid]) {
                if (target >= nums[low] && target < nums[mid]) {
                    high = mid - 1;
                } else {
                    low = mid + 1;
                }
            }
            // Right half is sorted
            else {
                if (target > nums[mid] && target <= nums[high]) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
        }

        return false;
    }
}
// Time Complexity: O(log N) average, O(N) worst case (e.g., [1, 1, 1, 1, 1]). Space Complexity: O(1).
```

---

#### Problem 11.4: Find Minimum in Rotated Sorted Array (LeetCode #153) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given the sorted rotated array `nums` of unique elements, return the **minimum element** of this array in $O(\log N)$ time.
* **Constraints**: $1 \le \text{nums.length} \le 5000$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.binarysearch;

public class FindMinRotatedSortedArray {
    public int findMin(int[] nums) {
        int low = 0, high = nums.length - 1;

        while (low < high) {
            int mid = low + (high - low) / 2;

            // If mid element is greater than high element, minimum MUST be strictly in the right half!
            if (nums[mid] > nums[high]) {
                low = mid + 1;
            } else {
                // Minimum is at mid or in left half
                high = mid;
            }
        }

        return nums[low];
    }
}
// Time Complexity: O(log N). Space Complexity: O(1).
```

---

#### Problem 11.5: Find First and Last Position of Element in Sorted Array (LeetCode #34) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of integers `nums` sorted in non-decreasing order, find the starting and ending position of a given `target` value. You must write an algorithm with $O(\log N)$ runtime complexity.
* **Constraints**: $0 \le \text{nums.length} \le 10^5$.

##### 2. ⚡ Optimal Solution (Two Dedicated Binary Searches)
```java
package com.leetcode.binarysearch;

public class FirstAndLastPosition {
    public int[] searchRange(int[] nums, int target) {
        int first = findBound(nums, target, true);
        if (first == -1) return new int[]{-1, -1};
        int last = findBound(nums, target, false);
        return new int[]{first, last};
    }

    private int findBound(int[] nums, int target, boolean isFirst) {
        int low = 0, high = nums.length - 1;
        int bound = -1;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            if (nums[mid] == target) {
                bound = mid;
                if (isFirst) {
                    high = mid - 1; // Keep searching towards left for earlier occurrence
                } else {
                    low = mid + 1;  // Keep searching towards right for later occurrence
                }
            } else if (nums[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return bound;
    }
}
// Time Complexity: 2 * O(log N) = O(log N). Space Complexity: O(1).
```

---

#### Problem 11.6: Search a 2D Matrix (LeetCode #74) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an $m \times n$ integer matrix `matrix` where each row is sorted and the first integer of each row is greater than the last integer of the previous row. Return `true` if `target` is in `matrix`. Must solve in $O(\log(m \cdot n))$ time.
* **Constraints**: $1 \le m, n \le 100$.

##### 2. ⚡ Optimal Solution (Virtual 1D Array Flattening)
```java
package com.leetcode.binarysearch;

public class Search2DMatrix {
    public boolean searchMatrix(int[][] matrix, int target) {
        int m = matrix.length;
        int n = matrix[0].length;
        int low = 0, high = m * n - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int row = mid / n;
            int col = mid % n;
            int midVal = matrix[row][col];

            if (midVal == target) {
                return true;
            } else if (midVal < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return false;
    }
}
// Time Complexity: O(log(M * N)). Space Complexity: O(1).
```

---

#### Problem 11.7: Search a 2D Matrix II (LeetCode #240) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Integers in each row are sorted in ascending from left to right, and integers in each column are sorted ascending from top to bottom. Search for `target` in $O(M + N)$ time.
* **Constraints**: $1 \le m, n \le 300$.

##### 2. ⚡ Optimal Solution (Top-Right Pointer Pruning)
```java
package com.leetcode.binarysearch;

public class Search2DMatrixII {
    public boolean searchMatrix(int[][] matrix, int target) {
        int row = 0;
        int col = matrix[0].length - 1; // Start at Top-Right Corner

        while (row < matrix.length && col >= 0) {
            if (matrix[row][col] == target) {
                return true;
            } else if (matrix[row][col] > target) {
                col--; // Target must be in a column further to the left
            } else {
                row++; // Target must be in a row further down
            }
        }

        return false;
    }
}
// Time Complexity: O(M + N). Space Complexity: O(1).
```

---

#### Problem 11.8: Find Peak Element (LeetCode #162) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: A peak element is an element that is strictly greater than its neighbors. Given an integer array `nums`, find a peak element, and return its index in $O(\log N)$ time.
* **Constraints**: $1 \le \text{nums.length} \le 1000$, $\text{nums}[i] \ne \text{nums}[i + 1]$.

##### 2. ⚡ Optimal Binary Search Solution
```java
package com.leetcode.binarysearch;

public class FindPeakElement {
    public int findPeakElement(int[] nums) {
        int low = 0, high = nums.length - 1;

        while (low < high) {
            int mid = low + (high - low) / 2;

            // If slope is decreasing, peak must be at mid or to the left
            if (nums[mid] > nums[mid + 1]) {
                high = mid;
            } else {
                // Slope is increasing, peak must be strictly to the right
                low = mid + 1;
            }
        }

        return low;
    }
}
// Time Complexity: O(log N). Space Complexity: O(1).
```

---

#### Problem 11.9: Koko Eating Bananas (LeetCode #875) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Koko loves bananas. There are $n$ piles of bananas. The guards will return in $h$ hours. Return the **minimum integer eating speed $k$** such that she can eat all bananas within $h$ hours.
* **Constraints**: $1 \le \text{piles.length} \le 10^4$, $\text{piles.length} \le h \le 10^9$.

##### 2. ⚡ Optimal Solution (Binary Search on Answer Space $[1 \dots \max(\text{piles})]$)
```java
package com.leetcode.binarysearch;

public class KokoEatingBananas {
    public int minEatingSpeed(int[] piles, int h) {
        int low = 1;
        int high = 1;
        for (int pile : piles) high = Math.max(high, pile);

        while (low < high) {
            int speed = low + (high - low) / 2;

            if (canFinish(piles, speed, h)) {
                high = speed; // Try smaller speed
            } else {
                low = speed + 1; // Speed too slow
            }
        }

        return low;
    }

    private boolean canFinish(int[] piles, int speed, int h) {
        long totalHours = 0;
        for (int pile : piles) {
            totalHours += (pile + speed - 1) / speed; // Ceiling division (pile / speed)
        }
        return totalHours <= h;
    }
}
// Time Complexity: O(N * log(max(piles))). Space Complexity: O(1).
```

---

#### Problem 11.10: Capacity To Ship Packages Within D Days (LeetCode #1011) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: A conveyor belt has packages that must be shipped within `days` days. Return the **least weight capacity** of the ship that will result in all packages being shipped within `days` days.
* **Constraints**: $1 \le \text{days} \le \text{weights.length} \le 5 \times 10^4$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.binarysearch;

public class ShipWithinDays {
    public int shipWithinDays(int[] weights, int days) {
        int low = 0;  // Max single package weight (minimum feasible capacity)
        int high = 0; // Sum of all package weights (maximum capacity needed)

        for (int w : weights) {
            low = Math.max(low, w);
            high += w;
        }

        while (low < high) {
            int capacity = low + (high - low) / 2;

            if (canShip(weights, capacity, days)) {
                high = capacity;
            } else {
                low = capacity + 1;
            }
        }

        return low;
    }

    private boolean canShip(int[] weights, int capacity, int maxDays) {
        int requiredDays = 1;
        int currentLoad = 0;

        for (int w : weights) {
            if (currentLoad + w > capacity) {
                requiredDays++;
                currentLoad = 0;
            }
            currentLoad += w;
        }

        return requiredDays <= maxDays;
    }
}
// Time Complexity: O(N * log(sum(weights) - max(weights))). Space Complexity: O(1).
```

### Pattern 12: Bitwise XOR & Bit Manipulation Pattern

```
====================== VISUAL BITWISE IDENTITIES CHEATSHEET ======================
XOR Properties:
1. Self-Inverse:           x ^ x = 0
2. Identity:               x ^ 0 = x
3. Commutative/Associative: a ^ b ^ a = (a ^ a) ^ b = 0 ^ b = b

Bitwise Masking Tricks:
- Clear lowest set bit:    n & (n - 1)          (Brian Kernighan's Trick)
- Isolate lowest set bit:  n & (-n)             (Extracts rightmost 1-bit)
- Check power of 2:        (n > 0) && ((n & (n - 1)) == 0)
- Toggle k-th bit:         n ^ (1 << k)
- Set k-th bit:            n | (1 << k)
- Clear k-th bit:          n & ~(1 << k)
- Check if k-th bit is set: (n & (1 << k)) != 0
==================================================================================
```

#### 🎯 Recognition Signals:
* Finding **unique/missing numbers** where other numbers appear twice, 3 times, or even times.
* Performing arithmetic (addition, subtraction, division) **without `+`, `-`, `*`, `/` operators**.
* Subsets generation via **bitmask integer iteration** ($0 \dots 2^N - 1$).

---

#### Problem 12.1: Single Number (LeetCode #136) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a non-empty array of integers `nums`, every element appears twice except for one. Find that single one. You must implement a solution with a linear runtime complexity and use only constant extra space.
* **Constraints**: $1 \le \text{nums.length} \le 3 \times 10^4$, $-3 \times 10^4 \le \text{nums}[i] \le 3 \times 10^4$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.bitmanipulation;

public class SingleNumber {
    public int singleNumber(int[] nums) {
        int unique = 0;
        for (int num : nums) {
            unique ^= num; // Duplicate numbers cancel out to 0!
        }
        return unique;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 12.2: Single Number II (LeetCode #137) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums` where every element appears **three times** except for one, which appears exactly once. Find the single element and return it in $O(N)$ time and $O(1)$ space.
* **Constraints**: $1 \le \text{nums.length} \le 3 \times 10^4$.

##### 2. ⚡ Optimal Solution (32-Bit Sum Modulo 3 / Digital Logic Gate Simulation)
```java
package com.leetcode.bitmanipulation;

public class SingleNumberII {
    // Approach A: 32-Bit Sum modulo 3 (Intuitive O(32 * N) Time, O(1) Space)
    public int singleNumber_BitCounts(int[] nums) {
        int result = 0;
        for (int bit = 0; bit < 32; bit++) {
            int bitSum = 0;
            for (int num : nums) {
                if (((num >> bit) & 1) == 1) {
                    bitSum++;
                }
            }
            if (bitSum % 3 != 0) {
                result |= (1 << bit);
            }
        }
        return result;
    }

    // Approach B: Two-State Finite State Machine (O(N) Time, O(1) Space)
    public int singleNumber_FSM(int[] nums) {
        int ones = 0, twos = 0;
        for (int num : nums) {
            ones = (ones ^ num) & ~twos;
            twos = (twos ^ num) & ~ones;
        }
        return ones;
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 12.3: Single Number III (LeetCode #260) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums`, in which exactly **two elements appear only once** and all the other elements appear exactly twice. Find the two elements that appear only once in $O(N)$ time and $O(1)$ space.
* **Constraints**: $2 \le \text{nums.length} \le 3 \times 10^4$.

##### 2. 👁️ Visual Rightmost Diff-Bit Partition Trace
```
nums = [ 1, 2, 1, 3, 2, 5 ]
Total XOR = 3 ^ 5 = (011)_2 ^ (101)_2 = (110)_2 = 6.
Lowest Set Bit (Diff Bit) = 6 & (-6) = (010)_2 = 2.
Group A (Bit 1 is Set):    [ 2, 2, 3 ] -> XOR = 3
Group B (Bit 1 is Not Set):[ 1, 1, 5 ] -> XOR = 5
Result: [ 3, 5 ]!
```

##### 3. ⚡ Optimal Solution
```java
package com.leetcode.bitmanipulation;

public class SingleNumberIII {
    public int[] singleNumber(int[] nums) {
        int xorSum = 0;
        for (int num : nums) {
            xorSum ^= num;
        }

        // Isolate lowest set bit (prevents 2's complement integer overflow on Integer.MIN_VALUE)
        int lowestSetBit = xorSum & (-xorSum);

        int a = 0, b = 0;
        for (int num : nums) {
            if ((num & lowestSetBit) != 0) {
                a ^= num; // Belongs to group with bit set
            } else {
                b ^= num; // Belongs to group with bit NOT set
            }
        }

        return new int[]{a, b};
    }
}
// Time Complexity: O(N). Space Complexity: O(1).
```

---

#### Problem 12.4: Counting Bits (LeetCode #338) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer $n$, return an array `ans` of length $n + 1$ such that for each $i$ ($0 \le i \le n$), `ans[i]` is the **number of 1's** in the binary representation of $i$. Must solve in $O(N)$ single pass.
* **Constraints**: $0 \le n \le 10^5$.

##### 2. ⚡ Optimal DP with Bit Manipulation Solution
```java
package com.leetcode.bitmanipulation;

public class CountingBits {
    public int[] countBits(int n) {
        int[] dp = new int[n + 1];

        // dp[i] = dp[i >> 1] + (i & 1)
        // Number of set bits in `i` equals set bits in `i/2` plus lowest bit
        for (int i = 1; i <= n; i++) {
            dp[i] = dp[i >> 1] + (i & 1);
        }

        return dp;
    }
}
// Time Complexity: O(N) single pass. Space Complexity: O(1) auxiliary (O(N) for output array).
```

---

#### Problem 12.5: Number of 1 Bits / Hamming Weight (LeetCode #191) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Write a function that takes the binary representation of a positive integer and returns the number of set bits it has (also known as the **Hamming weight**).
* **Constraints**: $1 \le n \le 2^{31} - 1$.

##### 2. ⚡ Optimal Solution (Brian Kernighan’s Algorithm)
```java
package com.leetcode.bitmanipulation;

public class HammingWeight {
    public int hammingWeight(int n) {
        int count = 0;

        // n & (n - 1) drops the lowest set bit in exactly 1 operation!
        while (n != 0) {
            n &= (n - 1);
            count++;
        }

        return count;
    }
}
// Time Complexity: O(Number of set bits) <= 32 operations. Space Complexity: O(1).
```

---

#### Problem 12.6: Reverse Bits (LeetCode #190) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Reverse bits of a given 32 bits unsigned integer.
* **Constraints**: The input is a 32-bit unsigned integer.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.bitmanipulation;

public class ReverseBits {
    public int reverseBits(int n) {
        int result = 0;

        for (int i = 0; i < 32; i++) {
            result <<= 1;          // Shift result left to make room for next bit
            result |= (n & 1);     // Extract lowest bit of n and add to result
            n >>>= 1;              // Unsigned logical right shift of n
        }

        return result;
    }
}
// Time Complexity: O(1) exactly 32 iterations. Space Complexity: O(1).
```

---

#### Problem 12.7: Sum of Two Integers (LeetCode #371) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given two integers `a` and `b`, return the sum of the two integers without using the operators `+` and `-`.
* **Constraints**: $-1000 \le a, b \le 1000$.

##### 2. ⚡ Optimal Solution (Half Adder Bitwise Arithmetic)
```java
package com.leetcode.bitmanipulation;

public class SumTwoIntegers {
    public int getSum(int a, int b) {
        while (b != 0) {
            int carry = (a & b) << 1; // Carry is generated where both bits are 1
            a = a ^ b;                // XOR performs addition without carry
            b = carry;                // Carry is added in the next cycle
        }
        return a;
    }
}
// Time Complexity: O(1) bounded by 32 bits. Space Complexity: O(1).
```

---

#### Problem 12.8: Bitwise AND of Numbers Range (LeetCode #201) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given two integers `left` and `right` that represent the range $[left, right]$, return the bitwise AND of all numbers in this range, inclusive.
* **Constraints**: $0 \le \text{left} \le \text{right} \le 2^{31} - 1$.

##### 2. ⚡ Optimal Solution (Find Common Binary Prefix)
```java
package com.leetcode.bitmanipulation;

public class BitwiseANDRange {
    public int rangeBitwiseAnd(int left, int right) {
        int shiftCount = 0;

        // Shift both numbers right until we find their common binary prefix
        while (left < right) {
            left >>= 1;
            right >>= 1;
            shiftCount++;
        }

        // Shift the common prefix back to its original bit positions
        return left << shiftCount;
    }
}
// Time Complexity: O(1) <= 32 iterations. Space Complexity: O(1).
```

---

#### Problem 12.9: Maximum XOR of Two Numbers in an Array (LeetCode #421) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums`, return the maximum result of `nums[i] XOR nums[j]`, where $0 \le i \le j < n$. Must solve in $O(N)$ time.
* **Constraints**: $1 \le \text{nums.length} \le 2 \times 10^5$, $0 \le \text{nums}[i] \le 2^{31} - 1$.

##### 2. ⚡ Optimal Solution (Binary Trie / Bitmask Prefix Set)
```java
package com.leetcode.bitmanipulation;

import java.util.HashSet;
import java.util.Set;

public class MaxXORPair {
    public int findMaximumXOR(int[] nums) {
        int maxXOR = 0;
        int mask = 0;

        // Test bits from most significant (bit 30) down to least significant (bit 0)
        for (int i = 30; i >= 0; i--) {
            mask |= (1 << i);
            Set<Integer> prefixes = new HashSet<>();

            for (int num : nums) {
                prefixes.add(num & mask);
            }

            // Greedy target: Can we set the i-th bit of maxXOR to 1?
            int candidate = maxXOR | (1 << i);

            for (int prefix : prefixes) {
                // If prefix ^ candidate exists in prefixes, candidate is achievable!
                if (prefixes.contains(prefix ^ candidate)) {
                    maxXOR = candidate;
                    break;
                }
            }
        }

        return maxXOR;
    }
}
// Time Complexity: O(32 * N) = O(N). Space Complexity: O(N) prefix set.
```

---

#### Problem 12.10: Subsets via Bit Manipulation (LeetCode #78 - Bitmask Variant) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Generate all $2^N$ subsets of an array `nums` iteratively using binary bitmasks from $0$ to $2^N - 1$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.bitmanipulation;

import java.util.ArrayList;
import java.util.List;

public class SubsetsBitmask {
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        int totalSubsets = 1 << nums.length; // 2^N

        for (int mask = 0; mask < totalSubsets; mask++) {
            List<Integer> subset = new ArrayList<>();
            for (int i = 0; i < nums.length; i++) {
                // If i-th bit is set in mask, include nums[i] in current subset
                if ((mask & (1 << i)) != 0) {
                    subset.add(nums[i]);
                }
            }
            result.add(subset);
        }

        return result;
    }
}
// Time Complexity: O(N * 2^N). Space Complexity: O(1) auxiliary space.
```

### Pattern 13: Top 'K' Elements Pattern (Heaps & QuickSelect)

```
====================== HEAP VS QUICKSELECT ARCHITECTURE ======================
Finding Top K Largest Elements from N items:

1. MIN-HEAP OF SIZE K (O(N log K) Time, O(K) Space - Streaming Friendly):
   - Maintain a Min-Heap capped at capacity K.
   - For every incoming number: heap.offer(num).
   - If heap.size() > K: heap.poll() (evicts smallest candidate).
   - Heap peek() always contains the K-th largest element!

2. QUICKSELECT / HOPCROFT SELECTION (O(N) Average Time, O(1) Auxiliary Space):
   - Partition array around pivot `p`.
   - If pivotIndex == targetIndex: found!
   - If pivotIndex < targetIndex: recurse strictly on right partition.
   - If pivotIndex > targetIndex: recurse strictly on left partition.
==============================================================================
```

#### 🎯 Recognition Signals:
* Finding the **$K$-th largest, $K$-th smallest, Top $K$ frequent**, or $K$ closest elements.
* When sorting the whole array takes $O(N \log N)$, but we only need the top $K$ ($O(N \log K)$ or $O(N)$).

#### 🛠️ Master Reusable Java Templates:
```java
// QuickSelect Template (O(N) Average Time, O(1) In-Place Space)
public int quickSelectTemplate(int[] nums, int left, int right, int k) {
    if (left == right) return nums[left];

    int pivotIndex = partition(nums, left, right);
    if (pivotIndex == k) {
        return nums[k];
    } else if (pivotIndex < k) {
        return quickSelectTemplate(nums, pivotIndex + 1, right, k);
    } else {
        return quickSelectTemplate(nums, left, pivotIndex - 1, k);
    }
}

private int partition(int[] nums, int left, int right) {
    int pivot = nums[right];
    int pIndex = left;
    for (int i = left; i < right; i++) {
        if (nums[i] <= pivot) {
            int temp = nums[i]; nums[i] = nums[pIndex]; nums[pIndex] = temp;
            pIndex++;
        }
    }
    int temp = nums[right]; nums[right] = nums[pIndex]; nums[pIndex] = temp;
    return pIndex;
}
```

---

#### Problem 13.1: Kth Largest Element in an Array (LeetCode #215) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums` and an integer $k$, return the $k$-th largest element in the array. Note that it is the $k$-th largest element in the sorted order, not the $k$-th distinct element. Can you solve it in $O(N)$ time without sorting?
* **Constraints**: $1 \le k \le \text{nums.length} \le 10^5$, $-10^4 \le \text{nums}[i] \le 10^4$.

##### 2. ⚡ Optimal Solutions (Min-Heap vs QuickSelect)
```java
package com.leetcode.topkelements;

import java.util.PriorityQueue;
import java.util.Random;

public class KthLargestElement {
    // Approach A: Min-Heap of Size K (O(N log K) Time, O(K) Space)
    public int findKthLargest_Heap(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(k);
        for (int num : nums) {
            minHeap.offer(num);
            if (minHeap.size() > k) {
                minHeap.poll(); // Evict smallest element among the top k+1
            }
        }
        return minHeap.peek();
    }

    // Approach B: Randomized QuickSelect (O(N) Average Time, O(1) Space)
    private static final Random random = new Random();

    public int findKthLargest_QuickSelect(int[] nums, int k) {
        int targetIndex = nums.length - k; // K-th largest is at index (N - k) in ascending order
        return quickSelect(nums, 0, nums.length - 1, targetIndex);
    }

    private int quickSelect(int[] nums, int left, int right, int k) {
        if (left == right) return nums[left];

        // Randomize pivot to protect against O(N^2) adversarial test cases
        int pivotIdx = left + random.nextInt(right - left + 1);
        swap(nums, pivotIdx, right);

        int pIndex = partition(nums, left, right);

        if (pIndex == k) {
            return nums[k];
        } else if (pIndex < k) {
            return quickSelect(nums, pIndex + 1, right, k);
        } else {
            return quickSelect(nums, left, pIndex - 1, k);
        }
    }

    private int partition(int[] nums, int left, int right) {
        int pivot = nums[right];
        int p = left;
        for (int i = left; i < right; i++) {
            if (nums[i] <= pivot) {
                swap(nums, i, p);
                p++;
            }
        }
        swap(nums, p, right);
        return p;
    }

    private void swap(int[] nums, int i, int j) {
        int temp = nums[i]; nums[i] = nums[j]; nums[j] = temp;
    }
}
// Time Complexity: O(N) average QuickSelect, O(N log K) Min-Heap. Space Complexity: O(1) QuickSelect.
```

---

#### Problem 13.2: Top K Frequent Elements (LeetCode #347) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums` and an integer $k$, return the $k$ most frequent elements. You may return the answer in any order. Your algorithm's time complexity must be better than $O(N \log N)$.
* **Constraints**: $1 \le \text{nums.length} \le 10^5$, $k \in [1, \text{number of unique elements}]$.

##### 2. ⚡ Optimal Solution (Bucket Sort $O(N)$ Time)
```java
package com.leetcode.topkelements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopKFrequentElements {
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> countMap = new HashMap<>();
        for (int num : nums) {
            countMap.put(num, countMap.getOrDefault(num, 0) + 1);
        }

        // Bucket array: index represents frequency count (from 0 to nums.length)
        List<Integer>[] buckets = new List[nums.length + 1];

        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            int freq = entry.getValue();
            if (buckets[freq] == null) {
                buckets[freq] = new ArrayList<>();
            }
            buckets[freq].add(entry.getKey());
        }

        int[] result = new int[k];
        int resultIdx = 0;

        // Traverse buckets backwards from highest frequency to lowest
        for (int freq = buckets.length - 1; freq >= 0 && resultIdx < k; freq--) {
            if (buckets[freq] != null) {
                for (int num : buckets[freq]) {
                    result[resultIdx++] = num;
                    if (resultIdx == k) break;
                }
            }
        }

        return result;
    }
}
// Time Complexity: O(N) linear time. Space Complexity: O(N) bucket array.
```

---

#### Problem 13.3: Kth Smallest Element in a Sorted Matrix (LeetCode #378) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an $n \times n$ matrix where each of the rows and columns is sorted in ascending order, return the $k$-th smallest element in the matrix.
* **Constraints**: $n == \text{matrix.length}$, $1 \le n \le 300$, $1 \le k \le n^2$.

##### 2. ⚡ Optimal Binary Search on Matrix Range Solution
```java
package com.leetcode.topkelements;

public class KthSmallestMatrix {
    public int kthSmallest(int[][] matrix, int k) {
        int n = matrix.length;
        int low = matrix[0][0];
        int high = matrix[n - 1][n - 1];

        while (low < high) {
            int mid = low + (high - low) / 2;
            int count = countLessOrEqual(matrix, mid, n);

            if (count < k) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        return low;
    }

    // Counts elements <= target in O(N) using staircase search starting from bottom-left
    private int countLessOrEqual(int[][] matrix, int target, int n) {
        int count = 0;
        int row = n - 1;
        int col = 0;

        while (row >= 0 && col < n) {
            if (matrix[row][col] <= target) {
                count += (row + 1); // All elements above in this column are also <= target
                col++;
            } else {
                row--;
            }
        }

        return count;
    }
}
// Time Complexity: O(N * log(max - min)). Space Complexity: O(1).
```

---

#### Problem 13.4: Find K Pairs with Smallest Sums (LeetCode #373) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given two integer arrays `nums1` and `nums2` sorted in ascending order and an integer $k$. Return the $k$ pairs $(u_1, v_1), (u_2, v_2), \dots, (u_k, v_k)$ with the **smallest sums**.
* **Constraints**: $1 \le \text{nums1.length}, \text{nums2.length} \le 10^5$, $1 \le k \le 10^4$.

##### 2. ⚡ Optimal Min-Heap (K-Way Merge Matrix) Solution
```java
package com.leetcode.topkelements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class KPairsSmallestSums {
    public List<List<Integer>> kSmallestPairs(int[] nums1, int[] nums2, int k) {
        List<List<Integer>> result = new ArrayList<>();
        if (nums1.length == 0 || nums2.length == 0 || k == 0) return result;

        // Min-Heap storing [nums1[i] + nums2[j], i, j]
        PriorityQueue<int[]> minHeap = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));

        // Seed heap with initial pairs: (nums1[i], nums2[0]) for i from 0 to min(k, nums1.length)
        for (int i = 0; i < Math.min(nums1.length, k); i++) {
            minHeap.offer(new int[]{nums1[i] + nums2[0], i, 0});
        }

        while (k > 0 && !minHeap.isEmpty()) {
            int[] curr = minHeap.poll();
            int i = curr[1];
            int j = curr[2];

            result.add(Arrays.asList(nums1[i], nums2[j]));
            k--;

            // If next element in nums2 exists for row i, add it to heap
            if (j + 1 < nums2.length) {
                minHeap.offer(new int[]{nums1[i] + nums2[j + 1], i, j + 1});
            }
        }

        return result;
    }
}
// Time Complexity: O(K log K). Space Complexity: O(min(K, N1)).
```

---

#### Problem 13.5: K Closest Points to Origin (LeetCode #973) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of `points` where `points[i] = [xi, yi]` represents a point on the X-Y plane and an integer $k$, return the $k$ closest points to the origin $(0, 0)$.
* **Constraints**: $1 \le k \le \text{points.length} \le 10^4$.

##### 2. ⚡ Optimal Solution (Max-Heap of Size K)
```java
package com.leetcode.topkelements;

import java.util.PriorityQueue;

public class KClosestPointsOrigin {
    public int[][] kClosest(int[][] points, int k) {
        // Max-Heap keeping points with largest distance at top
        PriorityQueue<int[]> maxHeap = new PriorityQueue<>((a, b) -> {
            int distA = a[0] * a[0] + a[1] * a[1];
            int distB = b[0] * b[0] + b[1] * b[1];
            return Integer.compare(distB, distA);
        });

        for (int[] pt : points) {
            maxHeap.offer(pt);
            if (maxHeap.size() > k) {
                maxHeap.poll(); // Evict point furthest from origin
            }
        }

        int[][] result = new int[k][2];
        for (int i = 0; i < k; i++) {
            result[i] = maxHeap.poll();
        }

        return result;
    }
}
// Time Complexity: O(N log K). Space Complexity: O(K).
```

---

#### Problem 13.6: Sort Characters By Frequency (LeetCode #451) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a string `s`, sort it in **decreasing order** based on the frequency of the characters.
* **Constraints**: $1 \le \text{s.length} \le 5 \times 10^5$.

##### 2. ⚡ Optimal Solution (Bucket Sort)
```java
package com.leetcode.topkelements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortCharactersByFrequency {
    public String frequencySort(String s) {
        Map<Character, Integer> count = new HashMap<>();
        for (char c : s.toCharArray()) count.put(c, count.getOrDefault(c, 0) + 1);

        List<Character>[] buckets = new List[s.length() + 1];
        for (char c : count.keySet()) {
            int freq = count.get(c);
            if (buckets[freq] == null) buckets[freq] = new ArrayList<>();
            buckets[freq].add(c);
        }

        StringBuilder sb = new StringBuilder();
        for (int freq = buckets.length - 1; freq > 0; freq--) {
            if (buckets[freq] != null) {
                for (char c : buckets[freq]) {
                    for (int i = 0; i < freq; i++) {
                        sb.append(c);
                    }
                }
            }
        }

        return sb.toString();
    }
}
// Time Complexity: O(N). Space Complexity: O(N).
```

---

#### Problem 13.7: Kth Largest Element in a Stream (LeetCode #703) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Design a class to find the $k$-th largest element in a stream. Implement `KthLargest(int k, int[] nums)` and `add(int val)`.
* **Constraints**: $1 \le k \le 10^4$, up to $10^4$ calls to `add`.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.topkelements;

import java.util.PriorityQueue;

public class KthLargest {
    private final PriorityQueue<Integer> minHeap;
    private final int k;

    public KthLargest(int k, int[] nums) {
        this.k = k;
        this.minHeap = new PriorityQueue<>(k);
        for (int num : nums) {
            add(num);
        }
    }

    public int add(int val) {
        minHeap.offer(val);
        if (minHeap.size() > k) {
            minHeap.poll();
        }
        return minHeap.peek();
    }
}
// Time Complexity: O(log K) per add() invocation. Space Complexity: O(K).
```

---

#### Problem 13.8: Maximum Frequency Stack (LeetCode #895) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Design a stack-like data structure to push elements, and pop the **most frequent element**. If there is a tie, pop the element closest to the top of the stack.
* **Constraints**: Up to $2 \times 10^4$ calls to `push` and `pop`.

##### 2. ⚡ Optimal Solution ($O(1)$ Time Map of Stacks)
```java
package com.leetcode.topkelements;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class FreqStack {
    private final Map<Integer, Integer> freqMap;
    private final Map<Integer, Deque<Integer>> groupMap;
    private int maxFreq;

    public FreqStack() {
        freqMap = new HashMap<>();
        groupMap = new HashMap<>();
        maxFreq = 0;
    }

    public void push(int val) {
        int f = freqMap.getOrDefault(val, 0) + 1;
        freqMap.put(val, f);
        maxFreq = Math.max(maxFreq, f);

        groupMap.computeIfAbsent(f, k -> new ArrayDeque<>()).push(val);
    }

    public int pop() {
        int val = groupMap.get(maxFreq).pop();
        freqMap.put(val, maxFreq - 1);

        if (groupMap.get(maxFreq).isEmpty()) {
            maxFreq--;
        }

        return val;
    }
}
// Time Complexity: O(1) for both push and pop. Space Complexity: O(N).
```

---

#### Problem 13.9: Rearrange String k Distance Apart (LeetCode #358) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a string `s` and an integer $k$, rearrange `s` such that the same characters are at least distance $k$ from each other. Return `""` if impossible.
* **Constraints**: $1 \le \text{s.length} \le 3 \times 10^5$, $0 \le k \le s.\text{length}$.

##### 2. ⚡ Optimal Solution (Max-Heap + Cooldown Queue of Size K)
```java
package com.leetcode.topkelements;

import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Queue;

public class RearrangeStringKDistanceApart {
    public String rearrangeString(String s, int k) {
        if (k <= 1) return s;

        int[] count = new int[26];
        for (char c : s.toCharArray()) count[c - 'a']++;

        PriorityQueue<int[]> maxHeap = new PriorityQueue<>((a, b) -> Integer.compare(b[1], a[1]));
        for (int i = 0; i < 26; i++) {
            if (count[i] > 0) {
                maxHeap.offer(new int[]{i, count[i]});
            }
        }

        Queue<int[]> cooldownQueue = new ArrayDeque<>();
        StringBuilder sb = new StringBuilder();

        while (!maxHeap.isEmpty()) {
            int[] curr = maxHeap.poll();
            sb.append((char) ('a' + curr[0]));
            curr[1]--;

            cooldownQueue.offer(curr);

            // Once cooldown queue reaches size k, release the oldest cooled character
            if (cooldownQueue.size() >= k) {
                int[] released = cooldownQueue.poll();
                if (released[1] > 0) {
                    maxHeap.offer(released);
                }
            }
        }

        return (sb.length() == s.length()) ? sb.toString() : "";
    }
}
// Time Complexity: O(N log 26) = O(N). Space Complexity: O(26) = O(1).
```

---

#### Problem 13.10: Least Number of Unique Integers after K Removals (LeetCode #1481) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of integers `arr` and an integer $k$. Find the **least number of unique integers** after removing exactly $k$ elements.
* **Constraints**: $1 \le \text{arr.length} \le 10^5$, $0 \le k \le \text{arr.length}$.

##### 2. ⚡ Optimal Greedy Solution (Min-Heap / Frequency Array)
```java
package com.leetcode.topkelements;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LeastUniqueIntegers {
    public int findLeastNumOfUniqueInts(int[] arr, int k) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int num : arr) map.put(num, map.getOrDefault(num, 0) + 1);

        int[] freqs = new int[map.size()];
        int idx = 0;
        for (int f : map.values()) freqs[idx++] = f;

        Arrays.sort(freqs); // Greedily remove elements with smallest frequencies first

        int uniqueCount = freqs.length;
        for (int f : freqs) {
            if (k >= f) {
                k -= f;
                uniqueCount--;
            } else {
                break;
            }
        }

        return uniqueCount;
    }
}
// Time Complexity: O(N log N) or O(N) using counting sort. Space Complexity: O(N).
```

### Pattern 14: K-Way Merge Pattern

```
====================== VISUAL K-WAY MERGE ARCHITECTURE ======================
Given K sorted arrays / lists:

List 0: [ 2,  6,  8 ]
List 1: [ 3,  6,  7 ]
List 2: [ 1,  3,  4 ]

Initialize MIN-HEAP with the first element of each list:
Min-Heap: [ (1, List 2), (2, List 0), (3, List 1) ]

Step 1: Poll min (1, List 2) -> Output: [ 1 ]
        Insert next from List 2: (3, List 2) -> Heap: [ (2, List 0), (3, List 1), (3, List 2) ]

Step 2: Poll min (2, List 0) -> Output: [ 1, 2 ]
        Insert next from List 0: (6, List 0) -> Heap: [ (3, List 1), (3, List 2), (6, List 0) ]

Sorted output merged across all lists in O(N log K) time!
=============================================================================
```

#### 🎯 Recognition Signals:
* Merging **$K$ sorted arrays, lists, or streams**.
* Finding the **smallest range covering at least one element from each of $K$ lists**.
* Finding the $K$-th ugly number or prime fraction by merging multiple generator streams.

---

#### Problem 14.1: Merge K Sorted Lists (LeetCode #23) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an array of $k$ linked-lists `lists`, each linked-list is sorted in ascending order. Merge all the linked-lists into one sorted linked-list and return it.
* **Constraints**: $0 \le k \le 10^4$, $0 \le \text{length of each list} \le 500$, total nodes $\le 10^4$.

##### 2. ⚡ Optimal Solution (Min-Heap of Size K)
```java
package com.leetcode.kwaymerge;

import java.util.PriorityQueue;

public class MergeKSortedLists {
    public ListNode mergeKLists(ListNode[] lists) {
        if (lists == null || lists.length == 0) return null;

        // Min-Heap comparing list nodes by val
        PriorityQueue<ListNode> minHeap = new PriorityQueue<>((a, b) -> Integer.compare(a.val, b.val));

        // 1. Seed heap with head node of each non-empty list
        for (ListNode node : lists) {
            if (node != null) {
                minHeap.offer(node);
            }
        }

        ListNode dummy = new ListNode(0);
        ListNode curr = dummy;

        // 2. Continually pop smallest node and insert its next node
        while (!minHeap.isEmpty()) {
            ListNode smallest = minHeap.poll();
            curr.next = smallest;
            curr = curr.next;

            if (smallest.next != null) {
                minHeap.offer(smallest.next);
            }
        }

        return dummy.next;
    }
}
// Time Complexity: O(N log K) where N is total number of nodes across all lists. Space Complexity: O(K) heap size.
```

---

#### Problem 14.2: Smallest Range Covering Elements from K Lists (LeetCode #632) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You have $k$ lists of sorted integers in non-decreasing order. Find the **smallest range** $[a, b]$ that includes at least one number from each of the $k$ lists.
* **Constraints**: $1 \le k \le 3500$, $1 \le \text{nums}[i].\text{length} \le 50$, $-10^5 \le \text{nums}[i][j] \le 10^5$.

##### 2. 👁️ Visual K-Way Min-Heap Range Tracking Trace
```
Lists:
0: [ 4, 10, 15, 24 ]
1: [ 0,  9, 12, 20 ]
2: [ 5, 18, 22, 30 ]

Initial Heap: [ (0, L1, idx0), (4, L0, idx0), (5, L2, idx0) ], currentMax = 5.
Current Window Range: [0, 5] (Range length = 5).
Poll (0, L1, idx0) -> Insert next from L1: (9, L1, idx1), currentMax = 9.
New Heap: [ (4, L0, idx0), (5, L2, idx0), (9, L1, idx1) ] -> Range: [4, 9] (Length = 5).
...
Smallest range discovered: [ 20, 24 ] (Length = 4)!
```

##### 3. ⚡ Optimal Solution
```java
package com.leetcode.kwaymerge;

import java.util.List;
import java.util.PriorityQueue;

public class SmallestRangeKLists {
    public int[] smallestRange(List<List<Integer>> nums) {
        int k = nums.size();
        // Min-Heap storing [val, listIndex, elementIndex]
        PriorityQueue<int[]> minHeap = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));

        int currentMax = Integer.MIN_VALUE;

        // Seed heap with first element of all k lists
        for (int i = 0; i < k; i++) {
            int val = nums.get(i).get(0);
            minHeap.offer(new int[]{val, i, 0});
            currentMax = Math.max(currentMax, val);
        }

        int rangeStart = 0;
        int rangeEnd = Integer.MAX_VALUE;

        while (minHeap.size() == k) {
            int[] minElement = minHeap.poll();
            int currentMin = minElement[0];
            int listIdx = minElement[1];
            int elemIdx = minElement[2];

            // Update smallest range if current [currentMin, currentMax] is strictly tighter
            if ((long) currentMax - currentMin < (long) rangeEnd - rangeStart) {
                rangeStart = currentMin;
                rangeEnd = currentMax;
            }

            // Push next element from the same list that had its min popped
            if (elemIdx + 1 < nums.get(listIdx).size()) {
                int nextVal = nums.get(listIdx).get(elemIdx + 1);
                minHeap.offer(new int[]{nextVal, listIdx, elemIdx + 1});
                currentMax = Math.max(currentMax, nextVal);
            }
        }

        return new int[]{rangeStart, rangeEnd};
    }
}
// Time Complexity: O(N log K) where N is total elements across all lists. Space Complexity: O(K) heap size.
```

---

#### Problem 14.3: Ugly Number II (LeetCode #264) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: An **ugly number** is a positive integer whose prime factors are limited to 2, 3, and 5. Given an integer $n$, return the $n$-th ugly number.
* **Constraints**: $1 \le n \le 1690$.

##### 2. ⚡ Optimal Solution (3-Pointer Merging DP in $O(N)$ Time)
```java
package com.leetcode.kwaymerge;

public class UglyNumberII {
    public int nthUglyNumber(int n) {
        int[] ugly = new int[n];
        ugly[0] = 1;

        int p2 = 0, p3 = 0, p5 = 0; // Pointers for multiples of 2, 3, 5

        for (int i = 1; i < n; i++) {
            int next2 = ugly[p2] * 2;
            int next3 = ugly[p3] * 3;
            int next5 = ugly[p5] * 5;

            int nextUgly = Math.min(next2, Math.min(next3, next5));
            ugly[i] = nextUgly;

            // Advance all matching pointers (handles duplicates e.g., 2*3 == 3*2 == 6)
            if (nextUgly == next2) p2++;
            if (nextUgly == next3) p3++;
            if (nextUgly == next5) p5++;
        }

        return ugly[n - 1];
    }
}
// Time Complexity: O(N). Space Complexity: O(N) DP array.
```

---

#### Problem 14.4: Super Ugly Number (LeetCode #313) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: A **super ugly number** is a positive integer whose prime factors are in the array `primes`. Given an integer $n$ and an array of integers `primes`, return the $n$-th super ugly number.
* **Constraints**: $1 \le n \le 10^5$, $1 \le \text{primes.length} \le 100$.

##### 2. ⚡ Optimal Solution (K-Way Pointer Min-Heap)
```java
package com.leetcode.kwaymerge;

import java.util.PriorityQueue;

public class SuperUglyNumber {
    public int nthSuperUglyNumber(int n, int[] primes) {
        int k = primes.length;
        int[] ugly = new int[n];
        ugly[0] = 1;

        // Min-Heap storing [candidateVal, primeValue, uglyIndexPointer]
        PriorityQueue<int[]> minHeap = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));

        for (int prime : primes) {
            minHeap.offer(new int[]{prime, prime, 0});
        }

        for (int i = 1; i < n; i++) {
            ugly[i] = minHeap.peek()[0];

            // Drain all equal values to avoid duplicates
            while (minHeap.peek()[0] == ugly[i]) {
                int[] curr = minHeap.poll();
                int prime = curr[1];
                int idx = curr[2] + 1;
                minHeap.offer(new int[]{ugly[idx] * prime, prime, idx});
            }
        }

        return ugly[n - 1];
    }
}
// Time Complexity: O(N log K). Space Complexity: O(N + K).
```

---

#### Problem 14.5: Merge Sorted Array (LeetCode #88) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given two integer arrays `nums1` and `nums2`, sorted in non-decreasing order, and two integers $m$ and $n$. Merge `nums2` into `nums1` as one sorted array in-place.
* **Constraints**: $0 \le m, n \le 200$, $\text{nums1.length} == m + n$.

##### 2. ⚡ Optimal Solution (Backward Three-Pointer Merge)
```java
package com.leetcode.kwaymerge;

public class MergeSortedArray {
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int p1 = m - 1;
        int p2 = n - 1;
        int writeIdx = m + n - 1;

        // Fill nums1 from the back to avoid overwriting existing elements
        while (p1 >= 0 && p2 >= 0) {
            if (nums1[p1] > nums2[p2]) {
                nums1[writeIdx--] = nums1[p1--];
            } else {
                nums1[writeIdx--] = nums2[p2--];
            }
        }

        // Copy remaining elements of nums2 if any
        while (p2 >= 0) {
            nums1[writeIdx--] = nums2[p2--];
        }
    }
}
// Time Complexity: O(M + N). Space Complexity: O(1) in-place.
```

---

#### Problem 14.6: K-th Smallest Prime Fraction (LeetCode #786) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given a sorted integer array `arr` containing 1 and prime numbers. For every $i < j$, we consider the fraction $\text{arr}[i] / \text{arr}[j]$. Return the $k$-th smallest fraction.
* **Constraints**: $2 \le \text{arr.length} \le 1000$, $1 \le k \le \text{arr.length} \times (\text{arr.length} - 1) / 2$.

##### 2. ⚡ Optimal Min-Heap K-Way Solution
```java
package com.leetcode.kwaymerge;

import java.util.PriorityQueue;

public class KthSmallestPrimeFraction {
    public int[] kthSmallestPrimeFraction(int[] arr, int k) {
        int n = arr.length;
        // Min-Heap storing fractions [numeratorIdx, denominatorIdx]
        PriorityQueue<int[]> minHeap = new PriorityQueue<>((a, b) ->
            Double.compare((double) arr[a[0]] / arr[a[1]], (double) arr[b[0]] / arr[b[1]])
        );

        // Seed heap with smallest fraction for each numerator: arr[i] / arr[n - 1]
        for (int i = 0; i < n - 1; i++) {
            minHeap.offer(new int[]{i, n - 1});
        }

        for (int step = 0; step < k - 1; step++) {
            int[] curr = minHeap.poll();
            int numIdx = curr[0];
            int denIdx = curr[1];

            if (denIdx - 1 > numIdx) {
                minHeap.offer(new int[]{numIdx, denIdx - 1});
            }
        }

        int[] result = minHeap.poll();
        return new int[]{arr[result[0]], arr[result[1]]};
    }
}
// Time Complexity: O(N log N + K log N). Space Complexity: O(N).
```

---

#### Problem 14.7: Median of Two Sorted Arrays (LeetCode #4) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given two sorted arrays `nums1` and `nums2` of size $m$ and $n$ respectively, return the **median** of the two sorted arrays. The overall run time complexity should be $O(\log(m+n))$.
* **Constraints**: $0 \le m, n \le 1000$, $1 \le m + n \le 2000$.

##### 2. ⚡ Optimal Binary Search Partition Solution
```java
package com.leetcode.kwaymerge;

public class MedianTwoSortedArrays {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        // Ensure nums1 is smaller array to guarantee O(log(min(M, N)))
        if (nums1.length > nums2.length) {
            return findMedianSortedArrays(nums2, nums1);
        }

        int m = nums1.length;
        int n = nums2.length;
        int low = 0, high = m;

        while (low <= high) {
            int partitionX = low + (high - low) / 2;
            int partitionY = (m + n + 1) / 2 - partitionX;

            int maxLeftX = (partitionX == 0) ? Integer.MIN_VALUE : nums1[partitionX - 1];
            int minRightX = (partitionX == m) ? Integer.MAX_VALUE : nums1[partitionX];

            int maxLeftY = (partitionY == 0) ? Integer.MIN_VALUE : nums2[partitionY - 1];
            int minRightY = (partitionY == n) ? Integer.MAX_VALUE : nums2[partitionY];

            if (maxLeftX <= minRightY && maxLeftY <= minRightX) {
                // Correct partition found!
                if ((m + n) % 2 == 1) {
                    return Math.max(maxLeftX, maxLeftY);
                } else {
                    return (Math.max(maxLeftX, maxLeftY) + Math.min(minRightX, minRightY)) / 2.0;
                }
            } else if (maxLeftX > minRightY) {
                high = partitionX - 1; // Move partitionX to the left
            } else {
                low = partitionX + 1;  // Move partitionX to the right
            }
        }

        throw new IllegalArgumentException();
    }
}
// Time Complexity: O(log(min(M, N))). Space Complexity: O(1).
```

---

#### Problem 14.8: Intersection of Two Arrays II (LeetCode #350) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given two integer arrays `nums1` and `nums2`, return an array of their intersection. Each element in the result must appear as many times as it shows in both arrays.
* **Constraints**: $1 \le \text{nums1.length}, \text{nums2.length} \le 1000$.

##### 2. ⚡ Optimal Solution (Two Pointers on Sorted Arrays)
```java
package com.leetcode.kwaymerge;

import java.util.Arrays;

public class IntersectionTwoArrays {
    public int[] intersect(int[] nums1, int[] nums2) {
        Arrays.sort(nums1);
        Arrays.sort(nums2);

        int p1 = 0, p2 = 0, writeIdx = 0;

        while (p1 < nums1.length && p2 < nums2.length) {
            if (nums1[p1] == nums2[p2]) {
                nums1[writeIdx++] = nums1[p1];
                p1++;
                p2++;
            } else if (nums1[p1] < nums2[p2]) {
                p1++;
            } else {
                p2++;
            }
        }

        return Arrays.copyOfRange(nums1, 0, writeIdx);
    }
}
// Time Complexity: O(M log M + N log N). Space Complexity: O(1) in-place overwrite.
```

---

#### Problem 14.9: Find K-th Smallest Pair Distance (LeetCode #719) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: The distance of a pair of integers $a$ and $b$ is $|a - b|$. Given an integer array `nums` and an integer $k$, return the $k$-th smallest distance among all pairs.
* **Constraints**: $2 \le \text{nums.length} \le 10^4$, $1 \le k \le N(N-1)/2$.

##### 2. ⚡ Optimal Binary Search + Sliding Window Solution
```java
package com.leetcode.kwaymerge;

import java.util.Arrays;

public class KthSmallestPairDistance {
    public int smallestDistancePair(int[] nums, int k) {
        Arrays.sort(nums);
        int low = 0;
        int high = nums[nums.length - 1] - nums[0];

        while (low < high) {
            int mid = low + (high - low) / 2;
            int count = countPairsWithDistanceLessThanOrEqual(nums, mid);

            if (count < k) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        return low;
    }

    private int countPairsWithDistanceLessThanOrEqual(int[] nums, int maxDist) {
        int count = 0;
        int left = 0;

        for (int right = 0; right < nums.length; right++) {
            while (nums[right] - nums[left] > maxDist) {
                left++;
            }
            count += (right - left);
        }

        return count;
    }
}
// Time Complexity: O(N log N + N log(maxDist)). Space Complexity: O(1).
```

---

#### Problem 14.10: Merge Intervals using Priority Queue (LeetCode #56 - Variant) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Merge all overlapping intervals in an unsorted stream using a Min-Heap priority queue based approach.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.kwaymerge;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MergeIntervalsHeap {
    public int[][] merge(int[][] intervals) {
        if (intervals.length <= 1) return intervals;

        PriorityQueue<int[]> minHeap = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
        for (int[] interval : intervals) minHeap.offer(interval);

        List<int[]> merged = new ArrayList<>();
        int[] current = minHeap.poll();

        while (!minHeap.isEmpty()) {
            int[] next = minHeap.poll();
            if (current[1] >= next[0]) {
                current[1] = Math.max(current[1], next[1]);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        return merged.toArray(new int[merged.size()][]);
    }
}
// Time Complexity: O(N log N). Space Complexity: O(N).
```

### Pattern 15: Monotonic Stack & Monotonic Queue Pattern

```
====================== MONOTONIC STACK & QUEUE ARCHITECTURE ======================
1. MONOTONIC INCREASING STACK (Bottom to Top is strictly increasing: 1, 3, 5, 8):
   - Used to find: Next Smaller Element (NSE) / Previous Smaller Element (PSE).
   - Pop condition: while (!stack.isEmpty() && stack.peek() > incomingElement).

2. MONOTONIC DECREASING STACK (Bottom to Top is strictly decreasing: 8, 5, 3, 1):
   - Used to find: Next Greater Element (NGE) / Previous Greater Element (PGE).
   - Pop condition: while (!stack.isEmpty() && stack.peek() < incomingElement).

3. MONOTONIC DOUBLE-ENDED QUEUE (DEQUE):
   - Used for Sliding Window Min/Max in O(N) linear time.
   - Evicts elements outside current window boundary from front.
   - Maintains sorted order by evicting smaller/larger items from back.
==================================================================================
```

#### 🎯 Recognition Signals:
* Finding the **next greater / smaller element** or **previous greater / smaller element** for every element in an array.
* Finding spans, histograms, stock prices, or boundaries where a value remains the minimum/maximum.
* Amortized $O(N)$ total time: Every element is pushed at most once and popped at most once!

---

#### Problem 15.1: Daily Temperatures (LeetCode #739) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of integers `temperatures` representing the daily temperatures, return an array `answer` such that `answer[i]` is the number of days you have to wait after the $i$-th day to get a warmer temperature. If there is no future day for which this is possible, keep `answer[i] == 0`.
* **Constraints**: $1 \le \text{temperatures.length} \le 10^5$, $30 \le \text{temperatures}[i] \le 100$.

##### 2. 👁️ Visual Monotonic Decreasing Stack Trace
```
Temperatures: [ 73, 74, 75, 71, 69, 72, 76, 73 ]
Index:          0   1   2   3   4   5   6   7

Stack stores indices of unresolved cooler days:
Day 0 (73): Push index 0 -> Stack: [0(73)]
Day 1 (74): 74 > 73 -> Pop 0, ans[0] = 1 - 0 = 1 day. Push 1 -> Stack: [1(74)]
Day 2 (75): 75 > 74 -> Pop 1, ans[1] = 2 - 1 = 1 day. Push 2 -> Stack: [2(75)]
Day 3 (71): 71 < 75 -> Push 3 -> Stack: [2(75), 3(71)]
Day 4 (69): 69 < 71 -> Push 4 -> Stack: [2(75), 3(71), 4(69)]
Day 5 (72): 72 > 69 -> Pop 4, ans[4] = 5 - 4 = 1.
            72 > 71 -> Pop 3, ans[3] = 5 - 3 = 2.
            Push 5 -> Stack: [2(75), 5(72)]
Day 6 (76): 76 > 72 -> Pop 5, ans[5] = 6 - 5 = 1.
            76 > 75 -> Pop 2, ans[2] = 6 - 2 = 4.
            Push 6 -> Stack: [6(76)]
Result: [ 1, 1, 4, 2, 1, 1, 0, 0 ]!
```

##### 3. ⚡ Optimal Solution
```java
package com.leetcode.monotonicstack;

import java.util.ArrayDeque;
import java.util.Deque;

public class DailyTemperatures {
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] answer = new int[n];
        Deque<Integer> stack = new ArrayDeque<>(); // Stores indices

        for (int currDay = 0; currDay < n; currDay++) {
            int currentTemp = temperatures[currDay];

            // Pop all past days that are strictly cooler than today
            while (!stack.isEmpty() && temperatures[stack.peek()] < currentTemp) {
                int prevDay = stack.pop();
                answer[prevDay] = currDay - prevDay;
            }

            stack.push(currDay);
        }

        return answer;
    }
}
// Time Complexity: O(N) amortized (each index pushed/popped <= 1 time). Space Complexity: O(N).
```

---

#### Problem 15.2: Next Greater Element I (LeetCode #496) - [Easy]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: The next greater element of some element $x$ in an array is the first greater element that is to the right of $x$ in the same array. Given two distinct arrays `nums1` and `nums2`, where `nums1` is a subset of `nums2`, return an array `ans` of the next greater element for each query in `nums1`.
* **Constraints**: $1 \le \text{nums1.length} \le \text{nums2.length} \le 1000$.

##### 2. ⚡ Optimal Solution (Monotonic Stack + Hash Map)
```java
package com.leetcode.monotonicstack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class NextGreaterElementI {
    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        Map<Integer, Integer> nextGreaterMap = new HashMap<>();
        Deque<Integer> stack = new ArrayDeque<>();

        for (int num : nums2) {
            while (!stack.isEmpty() && stack.peek() < num) {
                nextGreaterMap.put(stack.pop(), num);
            }
            stack.push(num);
        }

        int[] result = new int[nums1.length];
        for (int i = 0; i < nums1.length; i++) {
            result[i] = nextGreaterMap.getOrDefault(nums1[i], -1);
        }

        return result;
    }
}
// Time Complexity: O(N1 + N2). Space Complexity: O(N2).
```

---

#### Problem 15.3: Next Greater Element II (LeetCode #503 - Circular Array) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a circular integer array `nums` (i.e., the next element of `nums[nums.length - 1]` is `nums[0]`), return the next greater number for every element in `nums`. If it doesn't exist, return -1.
* **Constraints**: $1 \le \text{nums.length} \le 10^4$, $-10^9 \le \text{nums}[i] \le 10^9$.

##### 2. ⚡ Optimal Solution (Virtual Double-Length Pass $2N$)
```java
package com.leetcode.monotonicstack;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class NextGreaterElementII {
    public int[] nextGreaterElements(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);

        Deque<Integer> stack = new ArrayDeque<>(); // Stores indices

        // Simulate circular traversal by running through 2 * n indices
        for (int i = 0; i < 2 * n; i++) {
            int currentNum = nums[i % n];

            while (!stack.isEmpty() && nums[stack.peek()] < currentNum) {
                result[stack.pop()] = currentNum;
            }

            if (i < n) {
                stack.push(i);
            }
        }

        return result;
    }
}
// Time Complexity: O(N). Space Complexity: O(N).
```

---

#### Problem 15.4: Largest Rectangle in Histogram (LeetCode #84) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of integers `heights` representing the histogram's bar height where the width of each bar is 1, return the area of the **largest rectangle** in the histogram. Must solve in $O(N)$ time.
* **Constraints**: $1 \le \text{heights.length} \le 10^5$, $0 \le \text{heights}[i] \le 10^4$.

##### 2. 👁️ Visual Monotonic Increasing Stack Boundary Trace
```
Histogram heights: [ 2, 1, 5, 6, 2, 3 ]

For every bar i, we find its left boundary (PSE) and right boundary (NSE).
When bar of height 2 is popped at index 4 (incoming height 2 <= top height 6, 5):
- Popped bar 3 (height 6): Left limit = index 2 (height 5), Right limit = index 4 (height 2).
  Width = 4 - 2 - 1 = 1. Area = 6 * 1 = 6.
- Popped bar 2 (height 5): Left limit = index 1 (height 1), Right limit = index 4 (height 2).
  Width = 4 - 1 - 1 = 2. Area = 5 * 2 = 10! (MAX AREA)
```

##### 3. ⚡ Optimal Solution
```java
package com.leetcode.monotonicstack;

import java.util.ArrayDeque;
import java.util.Deque;

public class LargestRectangleHistogram {
    public int largestRectangleArea(int[] heights) {
        int n = heights.length;
        Deque<Integer> stack = new ArrayDeque<>(); // Monotonically increasing indices
        int maxArea = 0;

        for (int i = 0; i <= n; i++) {
            // Append virtual height 0 at index n to force flush all remaining bars from stack
            int currentHeight = (i == n) ? 0 : heights[i];

            while (!stack.isEmpty() && heights[stack.peek()] >= currentHeight) {
                int height = heights[stack.pop()];
                int width = stack.isEmpty() ? i : (i - stack.peek() - 1);
                maxArea = Math.max(maxArea, height * width);
            }

            stack.push(i);
        }

        return maxArea;
    }
}
// Time Complexity: O(N) single pass. Space Complexity: O(N).
```

---

#### Problem 15.5: Maximal Rectangle (LeetCode #85) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a `rows x cols` binary `matrix` filled with 0's and 1's, find the largest rectangle containing only 1's and return its area.
* **Constraints**: $1 \le \text{rows}, \text{cols} \le 200$.

##### 2. ⚡ Optimal Solution (2D Histogram DP + Monotonic Stack)
```java
package com.leetcode.monotonicstack;

import java.util.ArrayDeque;
import java.util.Deque;

public class MaximalRectangle {
    public int maximalRectangle(char[][] matrix) {
        if (matrix == null || matrix.length == 0) return 0;
        int cols = matrix[0].length;
        int[] heights = new int[cols];
        int maxArea = 0;

        for (char[] row : matrix) {
            // Build continuous histogram heights row by row
            for (int j = 0; j < cols; j++) {
                if (row[j] == '1') {
                    heights[j] += 1;
                } else {
                    heights[j] = 0; // Reset height to 0 when broken by '0'
                }
            }

            maxArea = Math.max(maxArea, calculateHistogramMaxArea(heights));
        }

        return maxArea;
    }

    private int calculateHistogramMaxArea(int[] heights) {
        int n = heights.length;
        Deque<Integer> stack = new ArrayDeque<>();
        int maxArea = 0;

        for (int i = 0; i <= n; i++) {
            int h = (i == n) ? 0 : heights[i];
            while (!stack.isEmpty() && heights[stack.peek()] >= h) {
                int height = heights[stack.pop()];
                int width = stack.isEmpty() ? i : (i - stack.peek() - 1);
                maxArea = Math.max(maxArea, height * width);
            }
            stack.push(i);
        }

        return maxArea;
    }
}
// Time Complexity: O(Rows * Cols). Space Complexity: O(Cols).
```

---

#### Problem 15.6: Online Stock Span (LeetCode #901) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Design an algorithm that collects daily price quotes for some stock and returns the **span** of that stock's price for the current day (maximum number of consecutive days for which the price was $\le$ today's price).
* **Constraints**: Up to $10^4$ calls to `next(int price)`.

##### 2. ⚡ Optimal Solution (Monotonic Decreasing Stack with Aggregate Spans)
```java
package com.leetcode.monotonicstack;

import java.util.ArrayDeque;
import java.util.Deque;

public class StockSpanner {
    // Stack stores [price, aggregatedSpan]
    private final Deque<int[]> stack;

    public StockSpanner() {
        stack = new ArrayDeque<>();
    }

    public int next(int price) {
        int span = 1;

        // Merge all previous consecutive prices <= current price
        while (!stack.isEmpty() && stack.peek()[0] <= price) {
            span += stack.pop()[1];
        }

        stack.push(new int[]{price, span});
        return span;
    }
}
// Time Complexity: O(1) amortized per next() call. Space Complexity: O(N).
```

---

#### Problem 15.7: Remove K Digits (LeetCode #402) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given string `num` representing a non-negative integer and an integer $k$, return the smallest possible integer after removing $k$ digits from `num`.
* **Constraints**: $1 \le k \le \text{num.length} \le 10^5$.

##### 2. ⚡ Optimal Greedy Monotonic Stack Solution
```java
package com.leetcode.monotonicstack;

import java.util.ArrayDeque;
import java.util.Deque;

public class RemoveKDigits {
    public String removeKdigits(String num, int k) {
        if (num.length() == k) return "0";

        Deque<Character> stack = new ArrayDeque<>();

        for (char digit : num.toCharArray()) {
            // Whenever current digit is smaller than stack top, pop stack top to make number smaller
            while (k > 0 && !stack.isEmpty() && stack.peek() > digit) {
                stack.pop();
                k--;
            }
            stack.push(digit);
        }

        // If k removals still remain, drop from the end (stack top)
        while (k > 0) {
            stack.pop();
            k--;
        }

        // Build string from bottom of stack
        StringBuilder sb = new StringBuilder();
        while (!stack.isEmpty()) {
            sb.append(stack.pollLast());
        }

        // Remove leading zeroes
        while (sb.length() > 1 && sb.charAt(0) == '0') {
            sb.deleteCharAt(0);
        }

        return sb.length() == 0 ? "0" : sb.toString();
    }
}
// Time Complexity: O(N). Space Complexity: O(N).
```

---

#### Problem 15.8: Sum of Subarray Minimums (LeetCode #907) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array of integers `arr`, find the sum of `min(b)`, where $b$ ranges over every (contiguous) subarray of `arr`. Since the answer may be large, return the answer **modulo $10^9 + 7$**.
* **Constraints**: $1 \le \text{arr.length} \le 3 \times 10^4$, $1 \le \text{arr}[i] \le 3 \times 10^4$.

##### 2. ⚡ Optimal Solution (PLE & NLE Monotonic Stack in $O(N)$ Time)
```java
package com.leetcode.monotonicstack;

import java.util.ArrayDeque;
import java.util.Deque;

public class SumSubarrayMinimums {
    public int sumSubarrayMins(int[] arr) {
        int n = arr.length;
        int MOD = 1_000_000_007;

        int[] ple = new int[n]; // Distance to Previous Less Element
        int[] nle = new int[n]; // Distance to Next Less Element

        Deque<Integer> stack = new ArrayDeque<>();

        // 1. Calculate PLE (Previous Less Element)
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && arr[stack.peek()] > arr[i]) {
                stack.pop();
            }
            ple[i] = stack.isEmpty() ? (i + 1) : (i - stack.peek());
            stack.push(i);
        }

        stack.clear();

        // 2. Calculate NLE (Next Less Element with >= to handle duplicate values without double counting)
        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && arr[stack.peek()] >= arr[i]) {
                stack.pop();
            }
            nle[i] = stack.isEmpty() ? (n - i) : (stack.peek() - i);
            stack.push(i);
        }

        // 3. Contribution of arr[i] is arr[i] * ple[i] * nle[i]
        long totalSum = 0;
        for (int i = 0; i < n; i++) {
            long count = (long) ple[i] * nle[i] % MOD;
            totalSum = (totalSum + count * arr[i]) % MOD;
        }

        return (int) totalSum;
    }
}
// Time Complexity: O(N). Space Complexity: O(N).
```

---

#### Problem 15.9: Sliding Window Maximum (LeetCode #239) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an array of integers `nums`, there is a sliding window of size $k$ which is moving from the very left of the array to the very right. Return the max sliding window in $O(N)$ time.
* **Constraints**: $1 \le \text{nums.length} \le 10^5$, $1 \le k \le \text{nums.length}$.

##### 2. ⚡ Optimal Solution (Monotonic Decreasing Deque)
```java
package com.leetcode.monotonicstack;

import java.util.ArrayDeque;
import java.util.Deque;

public class SlidingWindowMaxDeque {
    public int[] maxSlidingWindow(int[] nums, int k) {
        int n = nums.length;
        int[] result = new int[n - k + 1];
        int resIdx = 0;

        Deque<Integer> deque = new ArrayDeque<>(); // Stores indices with descending values

        for (int i = 0; i < n; i++) {
            // 1. Evict elements outside the current window [i - k + 1, i]
            while (!deque.isEmpty() && deque.peekFirst() < i - k + 1) {
                deque.pollFirst();
            }

            // 2. Maintain monotonic decreasing order by evicting smaller values from back
            while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i]) {
                deque.pollLast();
            }

            deque.offerLast(i);

            // 3. Record maximum once the first full window is formed
            if (i >= k - 1) {
                result[resIdx++] = nums[deque.peekFirst()];
            }
        }

        return result;
    }
}
// Time Complexity: O(N) linear time. Space Complexity: O(K) deque size.
```

---

#### Problem 15.10: Shortest Subarray with Sum at Least K (LeetCode #862) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums` and an integer $k$, return the length of the shortest non-empty subarray of `nums` with a sum of at least $k$. If there is no such subarray, return -1. Note that array contains negative numbers.
* **Constraints**: $1 \le \text{nums.length} \le 10^5$, $-10^5 \le \text{nums}[i] \le 10^5$, $1 \le k \le 10^9$.

##### 2. ⚡ Optimal Solution (Prefix Sums + Monotonic Increasing Deque)
```java
package com.leetcode.monotonicstack;

import java.util.ArrayDeque;
import java.util.Deque;

public class ShortestSubarraySumAtLeastK {
    public int shortestSubarray(int[] nums, int k) {
        int n = nums.length;
        long[] prefixSum = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefixSum[i + 1] = prefixSum[i] + nums[i];
        }

        int minLen = Integer.MAX_VALUE;
        Deque<Integer> deque = new ArrayDeque<>(); // Monotonically increasing prefix sums

        for (int i = 0; i <= n; i++) {
            // If window [deque.peekFirst(), i] sum >= k, record length and advance front
            while (!deque.isEmpty() && prefixSum[i] - prefixSum[deque.peekFirst()] >= k) {
                minLen = Math.min(minLen, i - deque.pollFirst());
            }

            // Maintain monotonic increasing prefix sums in deque
            while (!deque.isEmpty() && prefixSum[i] <= prefixSum[deque.peekLast()]) {
                deque.pollLast();
            }

            deque.offerLast(i);
        }

        return minLen == Integer.MAX_VALUE ? -1 : minLen;
    }
}
// Time Complexity: O(N). Space Complexity: O(N).
```

### Pattern 16: Topological Sort Pattern (Kahn's Algorithm & DFS)

```
====================== TOPOLOGICAL SORT ALGORITHM BLUEPRINT ======================
Directed Acyclic Graph (DAG) Linear Ordering:

1. KAHN'S ALGORITHM (BFS In-Degree Peeling):
   - Compute inDegree[u] for all vertices.
   - Enqueue all vertices with inDegree == 0 (no prerequisite dependencies).
   - While queue is not empty:
     * u = queue.poll(); topoOrder.add(u);
     * For each neighbor v of u:
       - inDegree[v]--;
       - If inDegree[v] == 0 -> queue.offer(v);
   - If topoOrder.size() != totalVertices -> Graph has a CYCLE!

2. DFS 3-COLORING CYCLE DETECTION:
   - State 0 (WHITE): Unvisited.
   - State 1 (GRAY):  Currently in recursion stack (Cycle detected if visited again!).
   - State 2 (BLACK): Fully visited and topologically ordered.
==================================================================================
```

#### 🎯 Recognition Signals:
* Tasks or courses with **prerequisite orderings / dependency chains**.
* Determining if a **circular dependency / dead-lock cycle** exists.
* Deriving lexicographical or unique linear sequences from comparison rules.

---

#### Problem 16.1: Course Schedule (LeetCode #207) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: There are a total of `numCourses` courses you have to take, labeled from `0` to `numCourses - 1`. You are given an array `prerequisites` where `prerequisites[i] = [ai, bi]` indicates that you must take course $b_i$ first if you want to take course $a_i$. Return `true` if you can finish all courses. Otherwise, return `false`.
* **Constraints**: $1 \le \text{numCourses} \le 2000$, $0 \le \text{prerequisites.length} \le 5000$.

##### 2. ⚡ Optimal Solution (Kahn's BFS Algorithm)
```java
package com.leetcode.topologicalsort;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class CourseSchedule {
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        int[] inDegree = new int[numCourses];
        List<List<Integer>> adj = new ArrayList<>(numCourses);
        for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());

        for (int[] pre : prerequisites) {
            int course = pre[0];
            int prerequisite = pre[1];
            adj.get(prerequisite).add(course);
            inDegree[course]++;
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < numCourses; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
            }
        }

        int processedCourses = 0;
        while (!queue.isEmpty()) {
            int curr = queue.poll();
            processedCourses++;

            for (int neighbor : adj.get(curr)) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        return processedCourses == numCourses;
    }
}
// Time Complexity: O(V + E). Space Complexity: O(V + E).
```

---

#### Problem 16.2: Course Schedule II (LeetCode #210) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Return the ordering of courses you should take to finish all courses. If there are many valid answers, return any of them. If it is impossible to finish all courses, return an empty array.
* **Constraints**: $1 \le \text{numCourses} \le 2000$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.topologicalsort;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class CourseScheduleII {
    public int[] findOrder(int numCourses, int[][] prerequisites) {
        int[] inDegree = new int[numCourses];
        List<List<Integer>> adj = new ArrayList<>(numCourses);
        for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());

        for (int[] pre : prerequisites) {
            adj.get(pre[1]).add(pre[0]);
            inDegree[pre[0]]++;
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < numCourses; i++) {
            if (inDegree[i] == 0) queue.offer(i);
        }

        int[] order = new int[numCourses];
        int index = 0;

        while (!queue.isEmpty()) {
            int curr = queue.poll();
            order[index++] = curr;

            for (int neighbor : adj.get(curr)) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        return (index == numCourses) ? order : new int[0];
    }
}
// Time Complexity: O(V + E). Space Complexity: O(V + E).
```

---

#### Problem 16.3: Alien Dictionary (LeetCode #269) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: There is a new alien language that uses the English alphabet. However, the order among letters is unknown to you. You are given a list of strings `words` from the alien language's dictionary sorted lexicographically. Return a string of the unique letters in the new alien language sorted in lexicographical increasing order. If invalid/cyclic, return `""`.
* **Constraints**: $1 \le \text{words.length} \le 100$, $1 \le \text{words}[i].\text{length} \le 100$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.topologicalsort;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class AlienDictionary {
    public String alienOrder(String[] words) {
        Map<Character, Set<Character>> adj = new HashMap<>();
        Map<Character, Integer> inDegree = new HashMap<>();

        // 1. Initialize graph nodes
        for (String word : words) {
            for (char c : word.toCharArray()) {
                inDegree.putIfAbsent(c, 0);
                adj.putIfAbsent(c, new HashSet<>());
            }
        }

        // 2. Extract edge ordering from adjacent words
        for (int i = 0; i < words.length - 1; i++) {
            String w1 = words[i];
            String w2 = words[i + 1];

            // Invalid prefix rule: "abc" coming before "ab" is invalid lexicographical order
            if (w1.length() > w2.length() && w1.startsWith(w2)) {
                return "";
            }

            int minLen = Math.min(w1.length(), w2.length());
            for (int j = 0; j < minLen; j++) {
                char parent = w1.charAt(j);
                char child = w2.charAt(j);

                if (parent != child) {
                    if (!adj.get(parent).contains(child)) {
                        adj.get(parent).add(child);
                        inDegree.put(child, inDegree.get(child) + 1);
                    }
                    break; // Only the first differing character determines relative order
                }
            }
        }

        // 3. BFS Topo Sort (Kahn's Algorithm)
        Queue<Character> queue = new ArrayDeque<>();
        for (char c : inDegree.keySet()) {
            if (inDegree.get(c) == 0) {
                queue.offer(c);
            }
        }

        StringBuilder sb = new StringBuilder();
        while (!queue.isEmpty()) {
            char curr = queue.poll();
            sb.append(curr);

            for (char neighbor : adj.get(curr)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // If cycle exists, output length will not match total unique characters
        return (sb.length() == inDegree.size()) ? sb.toString() : "";
    }
}
// Time Complexity: O(Total characters in all words + Unique characters). Space Complexity: O(1) bounded by 26 letters.
```

---

#### Problem 16.4: Minimum Height Trees (LeetCode #310) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: A tree is an undirected graph in which any two vertices are connected by exactly one path. Return a list of all **root labels** of Minimum Height Trees (MHTs).
* **Constraints**: $1 \le n \le 2 \times 10^4$, $\text{edges.length} == n - 1$.

##### 2. ⚡ Optimal Inward Leaf-Trimming Topo BFS Solution
```java
package com.leetcode.topologicalsort;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class MinimumHeightTrees {
    public List<Integer> findMinHeightTrees(int n, int[][] edges) {
        if (n == 1) return List.of(0);

        List<Set<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new HashSet<>());

        for (int[] edge : edges) {
            adj.get(edge[0]).add(edge[1]);
            adj.get(edge[1]).add(edge[0]);
        }

        // Collect all leaf nodes (degree == 1)
        Queue<Integer> leaves = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (adj.get(i).size() == 1) {
                leaves.offer(i);
            }
        }

        int remainingNodes = n;
        // Trim outer leaves layer-by-layer until <= 2 centroids remain
        while (remainingNodes > 2) {
            int leafCount = leaves.size();
            remainingNodes -= leafCount;

            for (int i = 0; i < leafCount; i++) {
                int leaf = leaves.poll();
                int neighbor = adj.get(leaf).iterator().next();
                adj.get(neighbor).remove(leaf);

                if (adj.get(neighbor).size() == 1) {
                    leaves.offer(neighbor);
                }
            }
        }

        return new ArrayList<>(leaves);
    }
}
// Time Complexity: O(V + E) = O(N). Space Complexity: O(N).
```

---

#### Problem 16.5: Sequence Reconstruction (LeetCode #444) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Check whether the integer sequence `nums` is the only sequence that can be uniquely reconstructed from the given array of sequences `sequences`.
* **Constraints**: $1 \le \text{nums.length} \le 10^4$.

##### 2. ⚡ Optimal Solution (Kahn's Queue Invariant: Queue Size Must Always == 1)
```java
package com.leetcode.topologicalsort;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class SequenceReconstruction {
    public boolean sequenceReconstruction(int[] nums, List<List<Integer>> sequences) {
        int n = nums.length;
        int[] inDegree = new int[n + 1];
        List<Set<Integer>> adj = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) adj.add(new HashSet<>());

        for (List<Integer> seq : sequences) {
            for (int i = 0; i < seq.size() - 1; i++) {
                int u = seq.get(i);
                int v = seq.get(i + 1);
                if (adj.get(u).add(v)) {
                    inDegree[v]++;
                }
            }
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 1; i <= n; i++) {
            if (inDegree[i] == 0) queue.offer(i);
        }

        int index = 0;
        while (!queue.isEmpty()) {
            // If queue contains more than 1 choice at any point, sequence is not unique!
            if (queue.size() > 1) return false;

            int curr = queue.poll();
            if (nums[index++] != curr) return false;

            for (int neighbor : adj.get(curr)) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        return index == n;
    }
}
// Time Complexity: O(V + E). Space Complexity: O(V + E).
```

---

#### Problem 16.6: Course Schedule IV (LeetCode #1462) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given `numCourses` and prerequisites. You are also given `queries` where `queries[j] = [uj, vj]`. For each query, answer whether course $u_j$ is a prerequisite of course $v_j$.
* **Constraints**: $2 \le \text{numCourses} \le 100$, $1 \le \text{queries.length} \le 10^4$.

##### 2. ⚡ Optimal Solution (Topological BFS Transitive Closure)
```java
package com.leetcode.topologicalsort;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class CourseScheduleIV {
    public List<Boolean> checkIfPrerequisite(int numCourses, int[][] prerequisites, int[][] queries) {
        boolean[][] isPre = new boolean[numCourses][numCourses];
        int[] inDegree = new int[numCourses];
        List<List<Integer>> adj = new ArrayList<>(numCourses);
        for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());

        for (int[] p : prerequisites) {
            adj.get(p[0]).add(p[1]);
            inDegree[p[1]]++;
            isPre[p[0]][p[1]] = true;
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < numCourses; i++) {
            if (inDegree[i] == 0) queue.offer(i);
        }

        while (!queue.isEmpty()) {
            int curr = queue.poll();

            for (int neighbor : adj.get(curr)) {
                // Propagate all transitive prerequisites of curr to neighbor
                for (int i = 0; i < numCourses; i++) {
                    if (isPre[i][curr]) {
                        isPre[i][neighbor] = true;
                    }
                }

                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        List<Boolean> result = new ArrayList<>(queries.length);
        for (int[] q : queries) {
            result.add(isPre[q[0]][q[1]]);
        }

        return result;
    }
}
// Time Complexity: O(V * (V + E) + Q). Space Complexity: O(V^2).
```

---

#### Problem 16.7: Parallel Courses (LeetCode #1136) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an integer $n$ and `relations`. In one semester, you can take any number of courses as long as you have taken all prerequisites in previous semesters. Return the **minimum number of semesters** needed to take all courses, or -1 if impossible.
* **Constraints**: $1 \le n \le 5000$.

##### 2. ⚡ Optimal Solution (BFS Level-by-Level Topo Sort)
```java
package com.leetcode.topologicalsort;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ParallelCourses {
    public int minimumSemesters(int n, int[][] relations) {
        int[] inDegree = new int[n + 1];
        List<List<Integer>> adj = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) adj.add(new ArrayList<>());

        for (int[] r : relations) {
            adj.get(r[0]).add(r[1]);
            inDegree[r[1]]++;
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 1; i <= n; i++) {
            if (inDegree[i] == 0) queue.offer(i);
        }

        int semesters = 0;
        int takenCourses = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();
            semesters++;

            for (int i = 0; i < size; i++) {
                int curr = queue.poll();
                takenCourses++;

                for (int neighbor : adj.get(curr)) {
                    inDegree[neighbor]--;
                    if (inDegree[neighbor] == 0) {
                        queue.offer(neighbor);
                    }
                }
            }
        }

        return (takenCourses == n) ? semesters : -1;
    }
}
// Time Complexity: O(V + E). Space Complexity: O(V + E).
```

---

#### Problem 16.8: Longest Increasing Path in a Matrix (LeetCode #329) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an $m \times n$ integers matrix, return the length of the **longest increasing path**. You may move up, down, left, or right.
* **Constraints**: $1 \le m, n \le 200$.

##### 2. ⚡ Optimal Topological / DFS Memoization Solution
```java
package com.leetcode.topologicalsort;

public class LongestIncreasingPath {
    private static final int[][] DIRS = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

    public int longestIncreasingPath(int[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        int[][] memo = new int[m][n];
        int maxPath = 0;

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                maxPath = Math.max(maxPath, dfs(matrix, i, j, memo));
            }
        }

        return maxPath;
    }

    private int dfs(int[][] matrix, int r, int c, int[][] memo) {
        if (memo[r][c] != 0) return memo[r][c];

        int maxLen = 1;
        for (int[] dir : DIRS) {
            int nr = r + dir[0];
            int nc = c + dir[1];

            if (nr >= 0 && nr < matrix.length && nc >= 0 && nc < matrix[0].length && matrix[nr][nc] > matrix[r][c]) {
                maxLen = Math.max(maxLen, 1 + dfs(matrix, nr, nc, memo));
            }
        }

        memo[r][c] = maxLen;
        return maxLen;
    }
}
// Time Complexity: O(M * N). Space Complexity: O(M * N).
```

---

#### Problem 16.9: Find Eventual Safe States (LeetCode #802) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: A directed graph of $n$ nodes is given. A node is a **terminal node** if there are no outgoing edges. A node is a **safe node** if every possible path starting from that node leads to a terminal node. Return an array containing all safe nodes in ascending order.
* **Constraints**: $1 \le n \le 10^4$.

##### 2. ⚡ Optimal Solution (DFS 3-Coloring State Cycle Detection)
```java
package com.leetcode.topologicalsort;

import java.util.ArrayList;
import java.util.List;

public class EventualSafeStates {
    // 0: Unvisited, 1: Visiting (In current path), 2: Safe (Terminal / All paths terminate)
    public List<Integer> eventualSafeNodes(int[][] graph) {
        int n = graph.length;
        int[] color = new int[n];
        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (dfsIsSafe(graph, i, color)) {
                result.add(i);
            }
        }

        return result;
    }

    private boolean dfsIsSafe(int[][] graph, int node, int[] color) {
        if (color[node] > 0) {
            return color[node] == 2; // Return true if already marked SAFE
        }

        color[node] = 1; // Mark VISITING

        for (int neighbor : graph[node]) {
            if (!dfsIsSafe(graph, neighbor, color)) {
                return false; // Found cycle!
            }
        }

        color[node] = 2; // Mark SAFE
        return true;
    }
}
// Time Complexity: O(V + E). Space Complexity: O(V).
```

---

#### Problem 16.10: Build a Matrix With Conditions (LeetCode #2392) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given a positive integer $k$, and 2D integer arrays `rowConditions` and `colConditions`. Build a $k \times k$ matrix containing numbers 1 to $k$ such that row & column orderings are strictly satisfied. Return empty matrix if impossible.
* **Constraints**: $2 \le k \le 400$, $1 \le \text{conditions.length} \le 10^4$.

##### 2. ⚡ Optimal Solution (Independent 2D Topological Sort)
```java
package com.leetcode.topologicalsort;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class BuildMatrixWithConditions {
    public int[][] buildMatrix(int k, int[][] rowConditions, int[][] colConditions) {
        List<Integer> rowOrder = topoSort(k, rowConditions);
        List<Integer> colOrder = topoSort(k, colConditions);

        if (rowOrder.size() < k || colOrder.size() < k) {
            return new int[0][0]; // Cycle detected in row or col constraints!
        }

        int[] colPos = new int[k + 1];
        for (int c = 0; c < k; c++) {
            colPos[colOrder.get(c)] = c;
        }

        int[][] matrix = new int[k][k];
        for (int r = 0; r < k; r++) {
            int val = rowOrder.get(r);
            int c = colPos[val];
            matrix[r][c] = val;
        }

        return matrix;
    }

    private List<Integer> topoSort(int k, int[][] conditions) {
        int[] inDegree = new int[k + 1];
        List<List<Integer>> adj = new ArrayList<>(k + 1);
        for (int i = 0; i <= k; i++) adj.add(new ArrayList<>());

        for (int[] cond : conditions) {
            adj.get(cond[0]).add(cond[1]);
            inDegree[cond[1]]++;
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 1; i <= k; i++) {
            if (inDegree[i] == 0) queue.offer(i);
        }

        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int curr = queue.poll();
            order.add(curr);

            for (int neighbor : adj.get(curr)) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        return order;
    }
}
// Time Complexity: O(K + RowConditions + ColConditions). Space Complexity: O(K^2).
```

### Pattern 17: Union-Find / Disjoint Set Union (DSU) Pattern

```
====================== UNION-FIND (DSU) WITH PATH COMPRESSION & RANK ======================
Optimizations:
1. PATH COMPRESSION: parent[x] = find(parent[x]) flattens tree height to O(1).
2. UNION BY RANK / SIZE: Attaches shallower tree under root of deeper tree.
Total Amortized Time Complexity: O(alpha(N)) ≈ O(1) Ackermann Inverse!

Visual Tree Flattening:
      (4)                      (4)
      /                       / | \
    (3)       find(1)       (1)(2)(3)
    /     ------------->
  (2)
  /
(1)
============================================================================================
```

#### 🎯 Recognition Signals:
* Dynamic **connectivity queries**, finding connected components in undirected graphs.
* **Cycle detection** in undirected graphs without building full adjacency lists.
* **Kruskal’s Minimum Spanning Tree (MST)** algorithm.

#### 🛠️ Master Reusable Java Template:
```java
public class UnionFind {
    private final int[] parent;
    private final int[] rank;
    private int count;

    public UnionFind(int n) {
        this.count = n;
        this.parent = new int[n];
        this.rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 1;
        }
    }

    public int find(int i) {
        if (parent[i] == i) return i;
        return parent[i] = find(parent[i]); // Path compression
    }

    public boolean union(int i, int j) {
        int rootI = find(i);
        int rootJ = find(j);
        if (rootI == rootJ) return false; // Already in the same component!

        // Union by rank
        if (rank[rootI] < rank[rootJ]) {
            parent[rootI] = rootJ;
        } else if (rank[rootI] > rank[rootJ]) {
            parent[rootJ] = rootI;
        } else {
            parent[rootJ] = rootI;
            rank[rootI]++;
        }
        count--;
        return true;
    }

    public int getCount() {
        return count;
    }
}
```

---

#### Problem 17.1: Number of Connected Components in an Undirected Graph (LeetCode #323) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You have a graph of $n$ nodes labeled from $0$ to $n - 1$. You are given an integer $n$ and an array `edges` where `edges[i] = [ai, bi]`. Return the total number of **connected components**.
* **Constraints**: $1 \le n \le 2000$, $1 \le \text{edges.length} \le 5000$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.unionfind;

public class NumberConnectedComponents {
    public int countComponents(int n, int[][] edges) {
        UnionFind uf = new UnionFind(n);
        for (int[] edge : edges) {
            uf.union(edge[0], edge[1]);
        }
        return uf.getCount();
    }
}
// Time Complexity: O(V + E * alpha(V)) ≈ O(V + E). Space Complexity: O(V).
```

---

#### Problem 17.2: Graph Valid Tree (LeetCode #261) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given $n$ nodes labeled from $0$ to $n - 1$ and a list of undirected `edges`, write a function to check whether these edges make up a **valid tree**.
* **Constraints**: $1 \le n \le 2000$, $0 \le \text{edges.length} \le 5000$.

##### 2. ⚡ Optimal Solution (DSU Tree Invariant: No Cycles & Exactly $N-1$ Edges)
```java
package com.leetcode.unionfind;

public class GraphValidTree {
    public boolean validTree(int n, int[][] edges) {
        // A tree of N vertices MUST have exactly N - 1 edges
        if (edges.length != n - 1) return false;

        UnionFind uf = new UnionFind(n);
        for (int[] edge : edges) {
            // If two nodes already share the same root, adding this edge creates a CYCLE!
            if (!uf.union(edge[0], edge[1])) {
                return false;
            }
        }

        return uf.getCount() == 1; // Graph must be fully connected into 1 component
    }
}
// Time Complexity: O(N * alpha(N)). Space Complexity: O(N).
```

---

#### Problem 17.3: Redundant Connection (LeetCode #684) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: In this problem, a tree is an undirected graph that is connected and has no cycles. Given a graph that started as a tree with $n$ nodes labeled from 1 to $n$, with one additional edge added. Return an edge that can be removed so that the resulting graph is a tree of $n$ nodes.
* **Constraints**: $n == \text{edges.length}$, $3 \le n \le 1000$.

##### 2. ⚡ Optimal Solution
```java
package com.leetcode.unionfind;

public class RedundantConnection {
    public int[] findRedundantConnection(int[][] edges) {
        int n = edges.length;
        UnionFind uf = new UnionFind(n + 1);

        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];

            // If u and v are already connected, this edge is redundant!
            if (!uf.union(u, v)) {
                return edge;
            }
        }

        return new int[0];
    }
}
// Time Complexity: O(N * alpha(N)) ≈ O(N). Space Complexity: O(N).
```

---

#### Problem 17.4: Accounts Merge (LeetCode #721) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a list of `accounts` where each element `accounts[i]` is a list of strings, where the first element is a name, and the rest are emails. Merge accounts belonging to the same person.
* **Constraints**: $1 \le \text{accounts.length} \le 1000$, $1 \le \text{accounts}[i].\text{length} \le 10$.

##### 2. ⚡ Optimal Solution (Email to Account ID Mapping DSU)
```java
package com.leetcode.unionfind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class AccountsMerge {
    public List<List<String>> accountsMerge(List<List<String>> accounts) {
        int n = accounts.size();
        UnionFind uf = new UnionFind(n);
        Map<String, Integer> emailToAccountId = new HashMap<>();

        // 1. Union account indices when they share the same email
        for (int i = 0; i < n; i++) {
            List<String> account = accounts.get(i);
            for (int j = 1; j < account.size(); j++) {
                String email = account.get(j);
                if (emailToAccountId.containsKey(email)) {
                    uf.union(i, emailToAccountId.get(email));
                } else {
                    emailToAccountId.put(email, i);
                }
            }
        }

        // 2. Group emails by component root account ID
        Map<Integer, TreeSet<String>> rootToEmails = new HashMap<>();
        for (Map.Entry<String, Integer> entry : emailToAccountId.entrySet()) {
            String email = entry.getKey();
            int accountId = entry.getValue();
            int root = uf.find(accountId);

            rootToEmails.computeIfAbsent(root, k -> new TreeSet<>()).add(email);
        }

        // 3. Format result
        List<List<String>> result = new ArrayList<>();
        for (Map.Entry<Integer, TreeSet<String>> entry : rootToEmails.entrySet()) {
            int rootAccountId = entry.getKey();
            String name = accounts.get(rootAccountId).get(0);

            List<String> mergedAccount = new ArrayList<>();
            mergedAccount.add(name);
            mergedAccount.addAll(entry.getValue());
            result.add(mergedAccount);
        }

        return result;
    }
}
// Time Complexity: O(N * K * log(N * K)). Space Complexity: O(N * K).
```

---

#### Problem 17.5: Satisfiability of Equality Equations (LeetCode #990) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an array of strings `equations` representing relationships between variables like `"a==b"` or `"b!=c"`. Return `true` if it is possible to assign integers to variable names so as to satisfy all the given equations.
* **Constraints**: $1 \le \text{equations.length} \le 500$, variables are lowercase english letters.

##### 2. ⚡ Optimal Solution (Two-Pass DSU)
```java
package com.leetcode.unionfind;

public class EqualityEquations {
    public boolean equationsPossible(String[] equations) {
        UnionFind uf = new UnionFind(26);

        // Pass 1: Union all variables connected by "=="
        for (String eq : equations) {
            if (eq.charAt(1) == '=') {
                int u = eq.charAt(0) - 'a';
                int v = eq.charAt(3) - 'a';
                uf.union(u, v);
            }
        }

        // Pass 2: Verify that "!=" relations do not share the same connected root
        for (String eq : equations) {
            if (eq.charAt(1) == '!') {
                int u = eq.charAt(0) - 'a';
                int v = eq.charAt(3) - 'a';
                if (uf.find(u) == uf.find(v)) {
                    return false; // Contradiction found!
                }
            }
        }

        return true;
    }
}
// Time Complexity: O(N * alpha(26)) = O(N). Space Complexity: O(1) 26 letters.
```

---

#### Problem 17.6: Surrounded Regions (LeetCode #130) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an $m \times n$ matrix `board` containing `'X'` and `'O'`, capture all regions that are 4-directionally surrounded by `'X'`. A region is captured by flipping all `'O'`s into `'X'`s in that surrounded region.
* **Constraints**: $1 \le m, n \le 200$.

##### 2. ⚡ Optimal Solution (DSU with Dummy Boundary Node $M \times N$)
```java
package com.leetcode.unionfind;

public class SurroundedRegionsDSU {
    public void solve(char[][] board) {
        if (board == null || board.length == 0) return;
        int m = board.length;
        int n = board[0].length;

        // Dummy node at index m * n represents outside/boundary connectedness
        int dummyRoot = m * n;
        UnionFind uf = new UnionFind(dummyRoot + 1);

        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (board[r][c] == 'O') {
                    int currentIdx = r * n + c;

                    // If 'O' is on the matrix border, connect it to dummyRoot
                    if (r == 0 || r == m - 1 || c == 0 || c == n - 1) {
                        uf.union(currentIdx, dummyRoot);
                    }

                    // Union with right neighbor
                    if (c + 1 < n && board[r][c + 1] == 'O') {
                        uf.union(currentIdx, r * n + (c + 1));
                    }
                    // Union with down neighbor
                    if (r + 1 < m && board[r + 1][c] == 'O') {
                        uf.union(currentIdx, (r + 1) * n + c);
                    }
                }
            }
        }

        // All 'O' cells not connected to dummyRoot are surrounded and must be flipped to 'X'
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (board[r][c] == 'O' && uf.find(r * n + c) != uf.find(dummyRoot)) {
                    board[r][c] = 'X';
                }
            }
        }
    }
}
// Time Complexity: O(M * N * alpha(M * N)). Space Complexity: O(M * N).
```

---

#### Problem 17.7: Min Cost to Connect All Points (LeetCode #1584) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an array `points` representing integer coordinates on a 2D plane. Return the minimum cost to make all points connected (Manhattan distance: $|x_i - x_j| + |y_i - y_j|$).
* **Constraints**: $1 \le \text{points.length} \le 1000$.

##### 2. ⚡ Optimal Solution (Kruskal’s MST with DSU)
```java
package com.leetcode.unionfind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinCostConnectPointsKruskal {
    public int minCostConnectPoints(int[][] points) {
        int n = points.length;
        List<int[]> edges = new ArrayList<>();

        // Generate all N * (N - 1) / 2 Manhattan edges
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int dist = Math.abs(points[i][0] - points[j][0]) + Math.abs(points[i][1] - points[j][1]);
                edges.add(new int[]{dist, i, j});
            }
        }

        // Sort edges by weight in ascending order
        edges.sort((a, b) -> Integer.compare(a[0], b[0]));

        UnionFind uf = new UnionFind(n);
        int totalCost = 0;
        int edgesConnected = 0;

        for (int[] edge : edges) {
            int weight = edge[0];
            int u = edge[1];
            int v = edge[2];

            if (uf.union(u, v)) {
                totalCost += weight;
                edgesConnected++;
                if (edgesConnected == n - 1) break; // MST complete!
            }
        }

        return totalCost;
    }
}
// Time Complexity: O(E log E) where E = N^2. Space Complexity: O(E).
```

---

#### Problem 17.8: Smallest String With Swaps (LeetCode #1202) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given a string `s`, and an array of pairs of indices `pairs` where `pairs[i] = [a, b]` indicates 2 indices of the string that you can swap any number of times. Return the **lexicographically smallest string** that `s` can be changed to.
* **Constraints**: $1 \le \text{s.length}, \text{pairs.length} \le 10^5$.

##### 2. ⚡ Optimal Solution (DSU Connected Indices Sorting)
```java
package com.leetcode.unionfind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class SmallestStringWithSwaps {
    public String smallestStringWithSwaps(String s, List<List<Integer>> pairs) {
        int n = s.length();
        UnionFind uf = new UnionFind(n);

        for (List<Integer> pair : pairs) {
            uf.union(pair.get(0), pair.get(1));
        }

        // Group characters of each connected component in a Min-Heap
        Map<Integer, PriorityQueue<Character>> componentChars = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = uf.find(i);
            componentChars.computeIfAbsent(root, k -> new PriorityQueue<>()).offer(s.charAt(i));
        }

        // Reconstruct string by polling smallest available character in each index's component
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int root = uf.find(i);
            sb.append(componentChars.get(root).poll());
        }

        return sb.toString();
    }
}
// Time Complexity: O((V + E) * alpha(V) + V log V). Space Complexity: O(V).
```

---

#### Problem 17.9: Longest Consecutive Sequence (LeetCode #128 - DSU Variant) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an unsorted array of integers `nums`, return the length of the **longest consecutive elements sequence** in $O(N)$ time.
* **Constraints**: $0 \le \text{nums.length} \le 10^5$.

##### 2. ⚡ Optimal DSU Solution
```java
package com.leetcode.unionfind;

import java.util.HashMap;
import java.util.Map;

public class LongestConsecutiveDSU {
    public int longestConsecutive(int[] nums) {
        if (nums.length == 0) return 0;

        Map<Integer, Integer> parent = new HashMap<>();
        Map<Integer, Integer> size = new HashMap<>();

        for (int num : nums) {
            if (parent.containsKey(num)) continue;

            parent.put(num, num);
            size.put(num, 1);

            // Union with left neighbor (num - 1)
            if (parent.containsKey(num - 1)) {
                union(num, num - 1, parent, size);
            }
            // Union with right neighbor (num + 1)
            if (parent.containsKey(num + 1)) {
                union(num, num + 1, parent, size);
            }
        }

        int maxLen = 0;
        for (int s : size.values()) {
            maxLen = Math.max(maxLen, s);
        }

        return maxLen;
    }

    private int find(int i, Map<Integer, Integer> parent) {
        if (parent.get(i) == i) return i;
        int root = find(parent.get(i), parent);
        parent.put(i, root);
        return root;
    }

    private void union(int i, int j, Map<Integer, Integer> parent, Map<Integer, Integer> size) {
        int rootI = find(i, parent);
        int rootJ = find(j, parent);
        if (rootI != rootJ) {
            parent.put(rootI, rootJ);
            size.put(rootJ, size.get(rootJ) + size.get(rootI));
        }
    }
}
// Time Complexity: O(N * alpha(N)) = O(N). Space Complexity: O(N).
```

---

#### Problem 17.10: Number of Islands II (LeetCode #305) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an empty 2D binary grid of size $m \times n$. An operation `positions[i] = [ri, ci]` turns water into land. Return an array of the number of islands after each add land operation dynamically.
* **Constraints**: $1 \le m, n \le 10^4$, $1 \le \text{positions.length} \le 10^4$.

##### 2. ⚡ Optimal Dynamic DSU Solution
```java
package com.leetcode.unionfind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NumberOfIslandsII {
    private static final int[][] DIRS = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

    public List<Integer> numIslands2(int m, int n, int[][] positions) {
        List<Integer> result = new ArrayList<>();
        int[] parent = new int[m * n];
        Arrays.fill(parent, -1); // -1 signifies water

        int count = 0;

        for (int[] pos : positions) {
            int r = pos[0];
            int c = pos[1];
            int idx = r * n + c;

            if (parent[idx] != -1) {
                result.add(count); // Already land
                continue;
            }

            parent[idx] = idx;
            count++;

            // Check 4 adjacent neighbors
            for (int[] dir : DIRS) {
                int nr = r + dir[0];
                int nc = c + dir[1];
                int nIdx = nr * n + nc;

                if (nr >= 0 && nr < m && nc >= 0 && nc < n && parent[nIdx] != -1) {
                    int rootNeighbor = find(nIdx, parent);
                    int rootCurrent = find(idx, parent);

                    if (rootNeighbor != rootCurrent) {
                        parent[rootCurrent] = rootNeighbor;
                        count--;
                    }
                }
            }

            result.add(count);
        }

        return result;
    }

    private int find(int i, int[] parent) {
        if (parent[i] == i) return i;
        return parent[i] = find(parent[i], parent);
    }
}
// Time Complexity: O(K * alpha(M * N)) ≈ O(K). Space Complexity: O(M * N).
```

### Pattern 18: Dynamic Programming (0/1 Knapsack, Unbounded, String & Interval DP)

```
====================== MASTER DP STATE SPACE REDUCTION MATRIX ======================
Type                     Transition Formula                          Space Optimization
------------------------------------------------------------------------------------
1D Linear (Fib/Robber)   dp[i] = max(dp[i-1], dp[i-2] + val[i])      O(N) -> O(1) two variables
0/1 Knapsack (Subsets)   dp[w] = dp[w] || dp[w - num]                Backward 1D loop: w from C down to num
Unbounded Knapsack (Coin) dp[w] = min(dp[w], dp[w - coin] + 1)       Forward 1D loop: w from coin up to C
String Grid (LCS/Edit)   dp[i][j] = match ? dp[i-1][j-1] : ...       2 Rows: dp[2][M] or 1 Row + prevDiag
Interval DP (Balloons)   dp[i][j] = max(dp[i][k-1] + dp[k+1][j] + ..) Loop length len from 1 to N
Tree DP (Robber III)     int[] dfs(Node) -> [robRoot, skipRoot]      O(H) recursion stack
State Machine (Stock)    hold[i] = max(hold[i-1], rest[i-1] - p)     O(1) state registers
====================================================================================
```

---

#### Problem 18.1: Partition Equal Subset Sum (LeetCode #416) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an integer array `nums`, return `true` if you can partition the array into two subsets such that the sum of the elements in both subsets is equal or `false` otherwise.
* **Constraints**: $1 \le \text{nums.length} \le 200$, $1 \le \text{nums}[i] \le 100$.

##### 2. ⚡ Optimal Solution (1D Backward 0/1 Knapsack)
```java
package com.leetcode.dp;

public class PartitionEqualSubsetSum {
    public boolean canPartition(int[] nums) {
        int totalSum = 0;
        for (int num : nums) totalSum += num;

        // An odd total sum can never be divided equally into two integer halves
        if (totalSum % 2 != 0) return false;

        int target = totalSum / 2;
        boolean[] dp = new boolean[target + 1];
        dp[0] = true; // Base case: target sum 0 is always achievable (empty set)

        for (int num : nums) {
            // Iterate BACKWARDS from target down to num to ensure each element is used at most ONCE
            for (int w = target; w >= num; w--) {
                dp[w] = dp[w] || dp[w - num];
            }
            if (dp[target]) return true; // Early exit
        }

        return dp[target];
    }
}
// Time Complexity: O(N * Target) where Target = Sum / 2. Space Complexity: O(Target).
```

---

#### Problem 18.2: Target Sum (LeetCode #494) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an integer array `nums` and an integer `target`. Build an expression by adding `+` or `-` before each integer and evaluate the expression. Return the number of different expressions that evaluate to `target`.
* **Constraints**: $1 \le \text{nums.length} \le 20$, $0 \le \text{nums}[i] \le 1000$, $-1000 \le \text{target} \le 1000$.

##### 2. ⚡ Optimal Mathematical Reduction to 0/1 Subset Sum
```java
package com.leetcode.dp;

public class TargetSum {
    // Mathematical Proof:
    // P = positive subset, N = negative subset
    // Sum(P) - Sum(N) = target
    // Sum(P) + Sum(N) = totalSum
    // Adding equations: 2 * Sum(P) = target + totalSum ==> Sum(P) = (target + totalSum) / 2
    public int findTargetSumWays(int[] nums, int target) {
        int totalSum = 0;
        for (int num : nums) totalSum += num;

        if (Math.abs(target) > totalSum || (target + totalSum) % 2 != 0) {
            return 0;
        }

        int subsetSum = (target + totalSum) / 2;
        int[] dp = new int[subsetSum + 1];
        dp[0] = 1; // 1 way to form sum 0

        for (int num : nums) {
            for (int w = subsetSum; w >= num; w--) {
                dp[w] += dp[w - num];
            }
        }

        return dp[subsetSum];
    }
}
// Time Complexity: O(N * SubsetSum). Space Complexity: O(SubsetSum).
```

---

#### Problem 18.3: Coin Change II (LeetCode #518) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an integer array `coins` representing coins of different denominations and an integer `amount`. Return the number of combinations that make up that amount (unbounded coin supply).
* **Constraints**: $1 \le \text{coins.length} \le 300$, $1 \le \text{coins}[i] \le 5000$, $0 \le \text{amount} \le 5000$.

##### 2. ⚡ Optimal Solution (1D Forward Unbounded Knapsack)
```java
package com.leetcode.dp;

public class CoinChangeII {
    public int change(int amount, int[] coins) {
        int[] dp = new int[amount + 1];
        dp[0] = 1;

        // Loop over coins on outer loop to count COMBINATIONS (order doesn't matter: {1, 2} == {2, 1})
        for (int coin : coins) {
            // Iterate FORWARD from coin up to amount to allow unlimited reuse of current coin
            for (int w = coin; w <= amount; w++) {
                dp[w] += dp[w - coin];
            }
        }

        return dp[amount];
    }
}
// Time Complexity: O(Coins.length * Amount). Space Complexity: O(Amount).
```

---

#### Problem 18.4: Longest Common Subsequence (LeetCode #1143) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given two strings `text1` and `text2`, return the length of their **longest common subsequence**.
* **Constraints**: $1 \le \text{text1.length}, \text{text2.length} \le 1000$.

##### 2. ⚡ Optimal Solution (Space-Optimized 1D Rolling Array)
```java
package com.leetcode.dp;

public class LongestCommonSubsequence {
    public int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];

        for (int i = 1; i <= m; i++) {
            char c1 = text1.charAt(i - 1);
            for (int j = 1; j <= n; j++) {
                char c2 = text2.charAt(j - 1);
                if (c1 == c2) {
                    curr[j] = 1 + prev[j - 1];
                } else {
                    curr[j] = Math.max(prev[j], curr[j - 1]);
                }
            }
            int[] temp = prev; prev = curr; curr = temp; // Swap rows
        }

        return prev[n];
    }
}
// Time Complexity: O(M * N). Space Complexity: O(min(M, N)).
```

---

#### Problem 18.5: Edit Distance (LeetCode #72) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given two strings `word1` and `word2`, return the minimum number of operations (insert, delete, replace) required to convert `word1` to `word2`.
* **Constraints**: $0 \le \text{word1.length}, \text{word2.length} \le 500$.

##### 2. ⚡ Optimal Levenshtein Distance DP Solution
```java
package com.leetcode.dp;

public class EditDistance {
    public int minDistance(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i; // Deletion cost
        for (int j = 0; j <= n; j++) dp[0][j] = j; // Insertion cost

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1]; // Characters match, 0 cost
                } else {
                    dp[i][j] = 1 + Math.min(
                        dp[i - 1][j - 1], // Replace
                        Math.min(
                            dp[i - 1][j], // Delete
                            dp[i][j - 1]  // Insert
                        )
                    );
                }
            }
        }

        return dp[m][n];
    }
}
// Time Complexity: O(M * N). Space Complexity: O(M * N) or O(N).
```

---

#### Problem 18.6: Longest Palindromic Substring (LeetCode #5) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given a string `s`, return the longest palindromic substring in `s`.
* **Constraints**: $1 \le \text{s.length} \le 1000$.

##### 2. ⚡ Optimal Expand Around Center Solution
```java
package com.leetcode.dp;

public class LongestPalindromicSubstring {
    public String longestPalindrome(String s) {
        if (s == null || s.length() < 1) return "";

        int start = 0, end = 0;

        for (int i = 0; i < s.length(); i++) {
            int len1 = expandAroundCenter(s, i, i);     // Odd length palindrome
            int len2 = expandAroundCenter(s, i, i + 1); // Even length palindrome
            int len = Math.max(len1, len2);

            if (len > end - start) {
                start = i - (len - 1) / 2;
                end = i + len / 2;
            }
        }

        return s.substring(start, end + 1);
    }

    private int expandAroundCenter(String s, int left, int right) {
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            left--;
            right++;
        }
        return right - left - 1;
    }
}
// Time Complexity: O(N^2). Space Complexity: O(1) in-place pointers.
```

---

#### Problem 18.7: Burst Balloons (LeetCode #312) - [Hard]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given $n$ balloons, indexed from $0$ to $n - 1$. Each balloon is painted with a number on it. If you burst balloon $i$ you get `nums[i - 1] * nums[i] * nums[i + 1]` coins. Return the maximum coins you can collect by bursting balloons wisely.
* **Constraints**: $1 \le n \le 300$, $0 \le \text{nums}[i] \le 100$.

##### 2. 👁️ Reverse Interval DP Intuition
Instead of picking the *first* balloon to burst (which destroys boundary independence), pick the **LAST balloon $k$ to burst** in interval $[i, j]$.

##### 3. ⚡ Optimal Solution
```java
package com.leetcode.dp;

public class BurstBalloons {
    public int maxCoins(int[] nums) {
        int n = nums.length;
        // Pad boundaries with virtual 1's: [1, nums[0], ..., nums[n-1], 1]
        int[] arr = new int[n + 2];
        arr[0] = 1;
        arr[n + 1] = 1;
        System.arraycopy(nums, 0, arr, 1, n);

        int[][] dp = new int[n + 2][n + 2];

        // Length of sub-interval
        for (int len = 1; len <= n; len++) {
            for (int i = 1; i <= n - len + 1; i++) {
                int j = i + len - 1;

                // Pick balloon k to be the LAST balloon burst in interval [i, j]
                for (int k = i; k <= j; k++) {
                    int coins = arr[i - 1] * arr[k] * arr[j + 1] + dp[i][k - 1] + dp[k + 1][j];
                    dp[i][j] = Math.max(dp[i][j], coins);
                }
            }
        }

        return dp[1][n];
    }
}
// Time Complexity: O(N^3). Space Complexity: O(N^2).
```

---

#### Problem 18.8: House Robber III (LeetCode #337) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: The thief found himself a new place for his thievery again. There is only one entrance to this area, called `root`. No two directly-linked houses can be broken into on the same night. Return the maximum amount of money the thief can rob without alerting the police.
* **Constraints**: $1 \le \text{nodes} \le 10^4$.

##### 2. ⚡ Optimal Tree DP Solution (Post-Order Bottom-Up)
```java
package com.leetcode.dp;

public class HouseRobberIII {
    public int rob(TreeNode root) {
        int[] result = robSub(root);
        return Math.max(result[0], result[1]);
    }

    // Returns int[] where:
    // index 0: max money if we ROB this node
    // index 1: max money if we SKIP this node
    private int[] robSub(TreeNode root) {
        if (root == null) return new int[]{0, 0};

        int[] left = robSub(root.left);
        int[] right = robSub(root.right);

        int[] res = new int[2];
        // 1. If we rob root, we MUST skip both left and right children
        res[0] = root.val + left[1] + right[1];

        // 2. If we skip root, we take max of robbing or skipping each child independently
        res[1] = Math.max(left[0], left[1]) + Math.max(right[0], right[1]);

        return res;
    }
}
// Time Complexity: O(N). Space Complexity: O(H) recursion call stack.
```

---

#### Problem 18.9: Best Time to Buy and Sell Stock with Cooldown (LeetCode #309) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: You are given an array `prices` where `prices[i]` is the price of a given stock on the $i$-th day. After you sell your stock, you cannot buy stock on the next day (i.e., 1 day cooldown). Maximize your profit.
* **Constraints**: $1 \le \text{prices.length} \le 5000$.

##### 2. ⚡ Optimal State Machine DP Solution ($O(1)$ Space)
```java
package com.leetcode.dp;

public class BestTimeStockCooldown {
    public int maxProfit(int[] prices) {
        if (prices == null || prices.length <= 1) return 0;

        int hold = -prices[0]; // Currently holding stock
        int sold = 0;          // Just sold stock today (entering cooldown)
        int rest = 0;          // In cooldown or idle

        for (int i = 1; i < prices.length; i++) {
            int prevHold = hold;
            int prevSold = sold;
            int prevRest = rest;

            hold = Math.max(prevHold, prevRest - prices[i]); // Buy from rest state or keep holding
            sold = prevHold + prices[i];                      // Sell stock
            rest = Math.max(prevRest, prevSold);              // Cooldown or remain resting
        }

        return Math.max(sold, rest);
    }
}
// Time Complexity: O(N). Space Complexity: O(1) registers.
```

---

#### Problem 18.10: Minimum Cost Tree From Leaf Values (LeetCode #1130) - [Medium]

##### 1. 📋 Problem Description & Constraints
* **Problem Statement**: Given an array `arr` of positive integers, consider the (binary) trees such that each node has either 0 or 2 children, the values of `arr` correspond to the values of each leaf in an in-order traversal, and the value of each non-leaf node is equal to the product of the largest leaf value in its left and right subtree respectively. Return the smallest possible sum of the values of each non-leaf node.
* **Constraints**: $2 \le \text{arr.length} \le 40$, $1 \le \text{arr}[i] \le 15$.

##### 2. ⚡ Optimal Greedy Monotonic Stack Solution ($O(N)$ Time)
```java
package com.leetcode.dp;

import java.util.ArrayDeque;
import java.util.Deque;

public class MinCostTreeLeafValues {
    public int mctFromLeafValues(int[] arr) {
        int result = 0;
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(Integer.MAX_VALUE); // Sentinel ceiling

        for (int num : arr) {
            // Drop smaller leaves by combining with their smallest immediate neighbor
            while (stack.peek() <= num) {
                int mid = stack.pop();
                result += mid * Math.min(stack.peek(), num);
            }
            stack.push(num);
        }

        while (stack.size() > 2) {
            result += stack.pop() * stack.peek();
        }

        return result;
    }
}
// Time Complexity: O(N). Space Complexity: O(N).
```

---

## 🏆 Comprehensive Master Pattern Summary Matrix

| # | Pattern Name | Key Data Structures | Primary Recognition Clue | Average Time | Aux Space | Top Classic LeetCode Problems |
|---|---|---|---|---|---|---|
| **1** | **Two Pointers** | Array / String Indices | Sorted array pairs, inward or outward collision | $O(N)$ | $O(1)$ | LC 167, 15, 11, 42, 75 |
| **2** | **Sliding Window** | HashMap / Frequency Array | Contiguous subarray/substring min/max/exact length | $O(N)$ | $O(K)$ | LC 3, 76, 424, 1004, 209 |
| **3** | **Fast & Slow Pointers** | Pointers / Floyd's Cycle | Linked list cycle, midpoint, palindrome, loop detection | $O(N)$ | $O(1)$ | LC 141, 142, 202, 287, 876 |
| **4** | **Merge Intervals** | Interval Arrays / Sort | Overlapping time slots, scheduling, coordinate meetings | $O(N \log N)$ | $O(N)$ | LC 56, 57, 435, 253, 452 |
| **5** | **Cyclic Sort** | Array $[1 \dots N]$ or $[0 \dots N]$ | Numbers in range $[1, N]$ with missing/duplicate items | $O(N)$ | $O(1)$ | LC 268, 448, 442, 645, 41 |
| **6** | **In-Place Linked List Reversal** | `prev`, `curr`, `next` Pointers | Reversing sub-segments of list in $O(1)$ memory | $O(N)$ | $O(1)$ | LC 206, 92, 25, 61, 24 |
| **7** | **Tree BFS (Level Order)** | `Queue<TreeNode>` | Level-by-level traversal, shortest path in unweighted graph | $O(N)$ | $O(W)$ | LC 102, 103, 199, 111, 127 |
| **8** | **Tree DFS (Post/Pre/In-Order)**| Recursion / Stack | Subtree properties, max path sum, diameter, ancestor queries | $O(N)$ | $O(H)$ | LC 112, 113, 124, 236, 98 |
| **9** | **Two Heaps** | `maxHeap` + `minHeap` | Continuous median tracking, dynamic dual partition | $O(\log N)$ | $O(N)$ | LC 295, 480, 502, 621 |
| **10** | **Backtracking & Subsets** | Recursion + Path State | Generating permutations, combinations, $N$-Queens, Sudoku | $O(2^N) / O(N!)$ | $O(N)$ | LC 78, 90, 46, 39, 51 |
| **11** | **Modified Binary Search** | `low`, `mid`, `high` Pointers | Sorted/rotated arrays, monotonic answer search spaces | $O(\log N)$ | $O(1)$ | LC 704, 33, 153, 34, 875 |
| **12** | **Bitwise XOR & Manipulation** | Bitmasks & Bit Operators | Unique elements, powers of 2, arithmetic without operators | $O(1)$ / $O(N)$ | $O(1)$ | LC 136, 137, 260, 338, 191 |
| **13** | **Top 'K' Elements** | Min/Max-Heap, QuickSelect | Top $K$ frequent, $K$-th largest/smallest, streaming $K$ items | $O(N \log K) / O(N)$ | $O(K)$ | LC 215, 347, 378, 973, 895 |
| **14** | **K-Way Merge** | Min-Heap of size $K$ | Merging $K$ sorted streams/lists, smallest covering range | $O(N \log K)$ | $O(K)$ | LC 23, 632, 264, 313, 4 |
| **15** | **Monotonic Stack / Queue** | `Deque<Integer>` (Indices) | Next/Previous Greater/Smaller Element, Histograms | $O(N)$ | $O(N)$ | LC 739, 496, 84, 85, 239 |
| **16** | **Topological Sort** | In-Degree Array + Queue | Prerequisite dependencies, cycle detection in DAGs | $O(V + E)$ | $O(V + E)$ | LC 207, 210, 269, 310, 1136 |
| **17** | **Union-Find (DSU)** | `parent[]` + `rank[]` | Dynamic connectivity, component counting, Kruskal's MST | $O(N \cdot \alpha(N))$ | $O(N)$ | LC 323, 261, 684, 721, 1584 |
| **18** | **Dynamic Programming (Knapsack/Interval)**| 1D/2D DP Tables | Overlapping subproblems, optimal substructure, Min/Max paths | $O(N \cdot C) / O(N^2)$ | $O(C) / O(N)$ | LC 416, 494, 518, 1143, 312 |

---

*Master LeetCode Design Patterns Reference Guide is complete with 180 comprehensive, compilable Java implementations, visual ASCII state diagrams, and exhaustive complexity trade-off analyses.*


















