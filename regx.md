[? Back to Home](README.md)

# 📚 The Definitive RegEx Engineering Guide: Core to Advanced

This document provides a comprehensive architectural breakdown of Regular Expression (RegEx) mechanics. It is designed for developers building high-performance parsers, compilers, or validation engines.

---

## 1. Engine Fundamentals & Character Logic

RegEx engines function as a **Finite State Automaton**. The engine moves a pointer through the input string based on the instructions provided in your pattern.

### 1.1 Metacharacters and Escaping
Metacharacters are symbols that carry functional logic. If you need to match the literal character, you must use the escape character `\`.
* **The Wildcard (`.`):** Matches any single character except line breaks.
* **Escaping:** To find a literal plus sign in a mathematical expression, use `\+`.
* **Example:** `\.com` matches the string ".com" but ignores "acom" or "bcom".

### 1.2 Character Classes `[]`
A character class allows you to define a set of characters, any **one** of which can trigger a match at that position.
* **Ranges:** `[a-z]` (any lowercase letter), `[0-9]` (any digit).
* **Negation `^`:** When placed as the first character inside brackets, it matches anything **not** in that set. `[^0-9]` matches all non-digits.
* **Performance Note:** Explicit character classes like `[a-zA-Z]` are faster for engines than the wildcard `.` because they provide a narrower search scope for the pointer.

### 1.3 Shorthand Reference Table
| Symbol | Equivalent | Technical Explanation |
| :--- | :--- | :--- |
| `\d` | `[0-9]` | Matches any numeric digit. |
| `\w` | `[a-zA-Z0-9_]` | Matches "Word" characters (letters, numbers, and underscores). |
| `\s` | `[ \t\r\n\f]` | Matches whitespace (spaces, tabs, carriage returns, line feeds). |
| `\b` | `Position` | **Word Boundary**: Matches the zero-width position between a `\w` and a `\W`. |

---

## 2. Quantifiers & The Backtracking Mechanism

Quantifiers determine how many times a character or group must repeat for a match to occur.

### 2.1 Standard Repetition
* `*` (Star): Match 0 or more times.
* `+` (Plus): Match 1 or more times.
* `?` (Question): Match 0 or 1 time (makes the preceding token optional).
* `{n}`: Exactly `n` times.
* `{n,m}`: Match at least `n` times but no more than `m` times.



### 2.2 Greedy vs. Lazy (Non-Greedy) Matching
By default, quantifiers are **Greedy**; they consume as much of the string as possible. Adding a `?` after a quantifier makes it **Lazy**, meaning it stops at the earliest possible match.

* **Target String:** `[Data A] some text [Data B]`
* **Greedy Pattern:** `\[.*\]`
    * **Logic:** The engine finds the first `[` and scans to the *very last* `]` in the string.
    * **Result:** `[Data A] some text [Data B]`
* **Lazy Pattern:** `\[.*?\]`
    * **Logic:** The engine finds the first `[` and stops at the *very next* `]`.
    * **Result:** `[Data A]`

---

## 3. Advanced Grouping & Memory Management

Groups allow you to isolate segments of data and apply logic (like OR operators) to multiple tokens.

### 3.1 Capturing Groups `(...)`
Everything inside parentheses is captured into a numbered memory slot (Group 1, Group 2, etc.).
* **Pattern:** `(\d{3})-(\d{3})` applied to `888-555`
* **Result:** Group 1 = `888`, Group 2 = `555`.

### 3.2 Non-Capturing Groups `(?:...)`
Groups tokens for logic but does **not** store the result in memory.
* **Architecture:** Use these to optimize performance and reduce memory overhead during heavy parsing.
* **Example:** `(?:https|ftp)://\w+` groups the protocols for the OR check but only captures the full URL.

### 3.3 Backreferences `\1`
This allows a pattern to refer back to a capture group matched earlier in the same string.
* **Example:** `(['"])(.*?)\1`
* **Logic:** If Group 1 captures a single quote `'`, the `\1` at the end forces the engine to look for a matching single quote, preventing mismatched quotes like `'text"`.

---

## 4. Zero-Width Assertions (Lookarounds)

Lookarounds are "non-consuming." They check if a condition is met but do not move the engine's pointer forward in the string.

### 4.1 Lookaheads (Forward Looking)
* **Positive Lookahead `(?=...)`**: Match X only if followed by Y.
    * **Pattern:** `\w+(?=\.tsx)`
    * **Example:** In `Main.tsx`, it matches `Main` but only because `.tsx` follows it.
* **Negative Lookahead `(?!...)`**: Match X only if NOT followed by Y.
    * **Pattern:** `\b(?!internal_)\w+`
    * **Example:** Matches words that do **not** begin with the prefix "internal_".

### 4.2 Lookbehinds (Backward Looking)
* **Positive Lookbehind `(?<=...)`**: Match X only if preceded by Y.
    * **Pattern:** `(?<=\$)\d+`
    * **Example:** In `$500`, it matches `500` because the `$` precedes it.
* **Negative Lookbehind `(?<!...)`**: Match X only if NOT preceded by Y.
    * **Pattern:** `(?<!-)\d+`
    * **Example:** Matches digits that are not part of a negative number (not preceded by `-`).



---

## 5. Daily Use Advanced Case Library

### 5.1 Strong Password Validation
**Logic:** Requires at least one lowercase, one uppercase, one digit, one special character, and a minimum length of 8.
```regex
^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$
```

### 5.2 SQL Query Parser (Tables & Columns)
**Logic:** Extracts column names and the target table from a SQL SELECT statement while ignoring case.

Code snippet
```regex
(?i)SELECT\s+(.+?)\s+FROM\s+([a-zA-Z_]\w*)
```
Explanation: (?i) makes it case-insensitive. Group 1 is columns, Group 2 is the table name. 

### 5.3 Environment Variable Parser
**Logic:** Parses .env files, handling single quotes, double quotes, and unquoted values.

Code snippet
```regex
^\s*([A-Z_]+)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^#\n\s]+))
```
Explanation: Captures Key in Group 1, and Value in Groups 2, 3, or 4 based on quoting.

### 5.4 Semantic Versioning (SemVer)
**Logic:** Validates version strings like 1.2.3-beta+build.10.


Code snippet
```regex
^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$
```
### 5.5 IP Address (IPv4) Strict Validation
**Logic:** Ensures each of the four octets is a valid number between 0 and 255.

Code snippet
```regex
^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$
```

### 6. Performance & Optimization
**Avoid Catastrophic Backtracking:** Never nest quantifiers like (a+)+. This can crash your app by creating exponential search paths.

**Anchor Patterns:** Use ^ (start) and $ (end) whenever possible to prevent the engine from starting a search at every single character in a long string.

**Atomic Groups:** (?>...): (If supported by your language) These "lock in" a match and prevent the engine from re-scanning if the rest of the pattern fails, greatly increasing speed.

# 🏆 The RegEx 100+ Scenario Masterclass

This guide uses scenario-based learning to move from fundamental character matching to complex architectural parsing.

---

## 📂 Category 1: Basic Character Matching (Scenarios 1-15)
*Focus: Literals, Wildcards, and Escaping.*

1.  **Exact Match:** Find the word "error" in a log file.
    * `error`
2.  **Case Insensitivity:** Find "ERROR", "Error", or "error".
    * `(?i)error`
3.  **Literal Period:** Match a file extension `.js`.
    * `\.js` (The `\` stops the `.` from being a wildcard).
4.  **Literal Plus:** Match a mathematical `+` in a string.
    * `\+`
5.  **Wildcard matching:** Match "cat", "cot", "c!t".
    * `c.t`
6.  **Matching a backslash:** Find a Windows path `C:\`.
    * `C:\\`
7.  **Match Start of Line:** Ensure a log starts with "INFO".
    * `^INFO`
8.  **Match End of Line:** Ensure a line ends with a semicolon `;`.
    * `;$`
9.  **Whitespace:** Match any single space or tab.
    * `\s`
10. **Non-Whitespace:** Match any character that isn't a space.
    * `\S`
11. **Word Characters:** Match any letter or number (Alphanumeric).
    * `\w`
12. **Non-Word Characters:** Match symbols like `@`, `#`, `!`.
    * `\W`
13. **Digit Matching:** Find any single number.
    * `\d`
14. **Non-Digit:** Find any character that isn't a number.
    * `\D`
15. **The Null Match:** Match an empty line.
    * `^$`

---

## 📂 Category 2: Character Sets & Ranges (Scenarios 16-30)
*Focus: Defining boundaries and exclusions.*

16. **Specific Vowels:** Match any vowel.
    * `[aeiou]`
17. **Consonants (Negation):** Match anything that is NOT a vowel.
    * `[^aeiou]`
18. **Lowercase Range:** Match any letter from a to z.
    * `[a-z]`
19. **Uppercase Range:** Match any letter from A to Z.
    * `[A-Z]`
20. **Hexadecimal:** Match characters allowed in Hex codes.
    * `[a-fA-F0-9]`
21. **Specific Symbols:** Match only `@` or `.`.
    * `[@.]`
22. **Alphanumeric Start:** Ensure a string starts with a letter.
    * `^[a-zA-Z]`
23. **Ending with Number:** Ensure a string ends with a digit.
    * `\d$`
24. **No Special Chars:** Match a string with no symbols.
    * `^[a-zA-Z0-9]*$`
25. **ASCII Range:** Match any standard ASCII character.
    * `[\x00-\x7F]`
26. **File Naming:** Match characters valid for a filename.
    * `[^\\/:*?"<>|]`
27. **Capitalized Words:** Match a word starting with an uppercase letter.
    * `[A-Z][a-z]*`
28. **Vowel and Digit:** Match a string that has one vowel followed by one digit.
    * `[aeiou]\d`
29. **Disallowing Spaces:** Match a string that has no spaces.
    * `^[^\s]*$`
30. **Match specific brackets:** Find literal `[` or `]`.
    * `[\[\]]`

---

## 📂 Category 3: Quantifiers & Repetition (Scenarios 31-50)
*Focus: Controlling length and frequency.*



31. **Optional Character:** Match "color" or "colour".
    * `colou?r`
32. **One or More:** Match a string of digits (at least one).
    * `\d+`
33. **Zero or More:** Match a word that might have trailing spaces.
    * `word\s*`
34. **Fixed Length:** Match exactly a 5-digit zip code.
    * `^\d{5}$`
35. **Range Length:** Match a username between 3 and 16 characters.
    * `^[a-z0-9_-]{3,16}$`
36. **Minimum Length:** Match a password with at least 8 characters.
    * `.{8,}`
37. **Greedy Matching:** Match everything between `<div>` and `</div>`.
    * `<div>.*</div>`
38. **Lazy Matching:** Match the first `<div>` tags in a list.
    * `<div>.*?</div>`
39. **Optional Prefix:** Match a phone number that might start with `+1`.
    * `(\+1)?\d{10}`
40. **Exactly Two:** Match a double-letter like "aa".
    * `([a-z])\1`
41. **Limit Repetitions:** Match a word with max 3 vowels in a row.
    * `[aeiou]{1,3}`
42. **Repeating Groups:** Match `abcabcabc`.
    * `(abc){3}`
43. **Phone Number Format:** Match `123-456-7890`.
    * `\d{3}-\d{3}-\d{4}`
44. **Price Tag:** Match `$10`, `$10.99`, or `$10.5`.
    * `\$\d+(\.\d{1,2})?`
45. **IP Segment:** Match a number from 0-999 (3 digits max).
    * `\d{1,3}`
46. **Binary String:** Match a string of 0s and 1s.
    * `^[01]+$`
47. **Multiple Newlines:** Match two or more consecutive newlines.
    * `\n{2,}`
48. **C-Style Comments:** Match `/* ... */`.
    * `\/\*[\s\S]*?\*\/`
49. **HTML Tags:** Match any opening tag `<p>`, `<h1>`.
    * `<[a-z0-9]+>`
50. **Leading Zeros:** Find numbers that start with `0`.
    * `^0\d+`

---

## 📂 Category 4: Grouping & Capturing (Scenarios 51-70)
*Focus: Data extraction and backreferences.*

51. **Simple Extraction:** Extract the domain from `email@domain.com`.
    * `@([a-z0-9.-]+)`
52. **Non-Capturing:** Group protocols but don't save them.
    * `(?:http|https|ftp)://`
53. **Named Groups:** Extract `year`, `month`, `day` (Language dependent).
    * `(?P<year>\d{4})-(?P<month>\d{2})-(?P<day>\d{2})`
54. **Nested Groups:** Match and capture area code and local number separately.
    * `((\d{3})-(\d{3}-\d{4}))`
55. **OR Logic:** Match "Apple" OR "Orange".
    * `(Apple|Orange)`
56. **Backreference duplicate:** Find duplicate words `the the`.
    * `(\b\w+)\s+\1`
57. **Quote Matching:** Match text inside `'single'` or `"double"` quotes.
    * `(['"])(.*?)\1`
58. **Protocol + Domain:** Extract components of `https://google.com`.
    * `(\w+):\/\/([^/]+)`
59. **Swapping Words:** Use groups to swap "First Last" to "Last, First".
    * `(\w+)\s(\w+)` -> Replace with `$2, $1`
60. **Strip HTML:** Find all tags to remove them.
    * `<[^>]*>`
61. **CSV Parsing:** Match columns in a CSV line.
    * `([^,]+)`
62. **MAC Address:** Match `00:1A:2B:3C:4D:5E`.
    * `([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})`
63. **Markdown Bold:** Match `**text**`.
    * `(\*\*|__)(.*?)\1`
64. **Environment Variables:** Match `KEY=VALUE`.
    * `^(\w+)=(.*)$`
65. **SQL Values:** Match values in `INSERT INTO (...) VALUES ('val1', 'val2')`.
    * `'([^']*)'`
66. **Time Format:** Match `HH:MM:SS`.
    * `(\d{2}):(\d{2}):(\d{2})`
67. **Log Date:** Extract date from `[2025-01-01 10:00]`.
    * `\[(\d{4}-\d{2}-\d{2})`
68. **URL Path:** Extract the path from `site.com/path/to/page`.
    * `site\.com/(.*)`
69. **Currency Code:** Match `100 USD`, `50 EUR`.
    * `(\d+)\s([A-Z]{3})`
70. **Version Extraction:** Get major/minor from `v1.2.3`.
    * `v(\d+)\.(\d+)`

---

## 📂 Category 5: Assertions & Lookarounds (Scenarios 71-90)
*Focus: Zero-width conditions (The "Advanced" stuff).*



71. **Positive Lookahead:** Match a word only if followed by a semicolon.
    * `\w+(?=;)`
72. **Negative Lookahead:** Match a word only if NOT followed by "ing".
    * `\b\w+\b(?!ing)`
73. **Positive Lookbehind:** Match digits only if they follow a `$`.
    * `(?<=\$)\d+`
74. **Negative Lookbehind:** Match numbers only if NOT preceded by a minus `-`.
    * `(?<!-)\d+`
75. **Strong Password (Lookahead combo):** Must have 1 digit and 1 uppercase.
    * `^(?=.*\d)(?=.*[A-Z]).{8,}$`
76. **Extracting content between specific tags:**
    * `(?<=<title>).*?(?=</title>)`
77. **Matching whole words only:**
    * `(?<=\s|^)word(?=\s|$)`
78. **Filter by extension:** Match filenames that are NOT `.exe`.
    * `^.*(?!\.exe$).*$`
79. **Complex URL Validation:** Ensure it has `https` and ends in `.com`.
    * `^(?=https).*?\.com$`
80. **Lookaround for decimal:** Match numbers with exactly two decimals.
    * `\d+(?=\.\d{2}$)`
81. **Lookahead for space:** Match words followed by a space (without including the space).
    * `\w+(?=\s)`
82. **Lookbehind for "Ref:":** Match ID in `Ref:12345`.
    * `(?<=Ref:)\d+`
83. **Excluding specific word:** Match any word except "admin".
    * `\b(?!admin\b)\w+\b`
84. **Multiple Lookarounds:** Ensure string has length 8-10 AND contains a symbol.
    * `^(?=.*[!@#$%^&*])(?=.{8,10}$).*$`
85. **Check for no digits:** String must contain no digits.
    * `^(?!\d+$).*$`
86. **Lookahead for camelCase humps:**
    * `[a-z](?=[A-Z])`
87. **Conditional Protocol:** Match domain only if `http` is present.
    * `(?<=http://)[a-z.]+`
88. **Negative Lookahead for file extension:** Everything except `.log` and `.txt`.
    * `^.*\.(?!(log|txt)$)[^.]+$`
89. **Match variable name:** Not starting with a digit.
    * `^(?!\d)\w+$`
90. **Lookahead for price:** Find digits followed by "USD".
    * `\d+(?=\s?USD)`

---

## 📂 Category 6: Real World Architecture (Scenarios 91-100+)
*Focus: Complex strings and edge cases.*

91. **Full Email (RFC 5322):**
    * `[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?`
92. **IPv4 Validation (Strict 0-255):**
    * `^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$`
93. **Removing Comment Blocks from Source:**
    * `(\/\*[\s\S]*?\*\/|\/\/.+)`
94. **JSON Key-Value Parser:**
    * `"([^"]+)":\s*"([^"]*)"`
95. **YouTube URL ID Extractor:**
    * `(?:v=|\/)([0-9A-Za-z_-]{11}).*`
96. **CSS Hex Colors (3 or 6 chars):**
    * `^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$`
97. **Extracting Attributes from HTML:**
    * `(\w+)\s*=\s*["']([^"']*)["']`
98. **Validating UUID/GUID:**
    * `^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$`
99. **Finding "TODO" in code:**
    * `(?i)\/\/.*TODO.*`
100. **CamelCase to Snake_Case Humps:**
    * `([a-z])([A-Z])` -> Replace with `$1_$2`
101. **Removing Leading/Trailing Whitespace:**
    * `^\s+|\s+$`
102. **Extracting Markdown Links:**
    * `\[([^\]]+)\]\(([^)]+)\)`

---

## 🛠️ Performance Tip
* **Avoid Nested Quantifiers:** `(a+)+` can crash your app (Catastrophic Backtracking).
* **Be Specific:** `[^"]*` is much faster than `.*?` for matching quoted strings.

# 🏆 The RegEx 100+ Scenario Masterclass (Multi-Language Edition)

This guide provides a comprehensive scenario-based roadmap for mastering Regular Expressions. It includes specific implementation logic for **JavaScript**, **Java**, and **Python**.

---

## 🛠️ Language Implementation Guide

### 1. JavaScript (Web / Tauri / Node.js)
In JavaScript, you use `/pattern/flags` or the `RegExp` object.
- **Global Match:** Use the `g` flag to find all occurrences.
- **Case Insensitive:** Use the `i` flag.

```javascript
const pattern = /"([^"]+)"/g;
const str = 'User "JohnDoe" logged in';
let match = pattern.exec(str);
if (match) console.log(match[1]); // Output: JohnDoe
```

I understand completely. To be absolutely clear: you want the entire guide, including all the explanations, the code for JavaScript, Java, and Python, and the 100+ scenarios, to be contained strictly inside a single code block so you can save it as README.md.

Here is the complete, high-detail document in that exact format:

Markdown

# 🏆 The RegEx 100+ Scenario Masterclass (Multi-Language Edition)

This guide provides a comprehensive scenario-based roadmap for mastering Regular Expressions. It includes specific implementation logic for **JavaScript**, **Java**, and **Python**.

---

## 🛠️ Language Implementation Guide

### 1. JavaScript (Web / Tauri / Node.js)
In JavaScript, you use `/pattern/flags` or the `RegExp` object.
- **Global Match:** Use the `g` flag to find all occurrences.
- **Case Insensitive:** Use the `i` flag.

```javascript
const pattern = /"([^"]+)"/g;
const str = 'User "JohnDoe" logged in';
let match = pattern.exec(str);
if (match) console.log(match[1]); // Output: JohnDoe
``

### 2. Java (Spring Boot / Desktop)
Java requires double-escaping backslashes (e.g., \\d) and uses the java.util.regex package.

```java

import java.util.regex.*;

public class RegExProcessor {
    public static void main(String[] args) {
        String input = "User \"JohnDoe\" logged in";
        Pattern p = Pattern.compile("\"([^\"]+)\"");
        Matcher m = p.matcher(input);
        if (m.find()) {
            System.out.println(m.group(1)); // Output: JohnDoe
        }
    }
}
```
### 3. Python (Scripts / Backend)
Python uses the re module. Raw strings (r'...') are used to prevent Python from interpreting backslashes.

```Python

import re

input_string = 'User "JohnDoe" logged in'
match = re.search(r'"([^"]+)"', input_string)
if match:
    print(match.group(1)) # Output: JohnDoe
```


# 🏆 The RegEx 100+ Scenario Masterclass

This guide provides a comprehensive scenario-based roadmap for mastering Regular Expressions.

---

## 📂 Category 1: Basic Character Matching (1-15)
**Focus:** Literals, Wildcards, and Escaping.

* **Exact Match:** `error` — Finds the specific string "error".
* **Case Insensitivity:** `(?i)error` — Matches Error, ERROR, or error.
* **Literal Period:** `\.js` — Escapes the dot (wildcard) to match a file extension.
* **Literal Plus:** `\+` — Matches a mathematical plus sign.
* **Wildcard (cat/cot/c!t):** `c.t` — The dot matches any single character.
* **Windows path:** `C:\\` — Matches a literal backslash in a path string.
* **Start of Line:** `^INFO` — Matches only if "INFO" starts the line.
* **End of Line:** `;$` — Matches only if ";" ends the line.
* **Whitespace:** `\s` — Matches spaces, tabs, and newlines.
* **Non-Whitespace:** `\S` — Matches any character that is NOT a space.
* **Alphanumeric:** `\w` — Matches letters, numbers, and underscores.
* **Non-Word:** `\W` — Matches symbols like @, !, #.
* **Digit:** `\d` — Matches any single number [0-9].
* **Non-Digit:** `\D` — Matches anything that is NOT a number.
* **Empty Line:** `^$` — Matches a line with no content.

---

## 📂 Category 2: Character Sets & Ranges (16-30)
**Focus:** Defining boundaries and exclusions.

* **Vowels:** `[aeiou]` — Matches any one vowel.
* **Consonants (Negation):** `[^aeiou]` — Matches any non-vowel.
* **Lowercase Range:** `[a-z]` — Matches any letter from a to z.
* **Uppercase Range:** `[A-Z]` — Matches any letter from A to Z.
* **Hexadecimal:** `[a-fA-F0-9]` — Matches valid hex digits.
* **Specific Symbols:** `[@.]` — Matches either @ or ..
* **Letter Start:** `^[a-zA-Z]` — Forces the first character to be a letter.
* **Ending with Number:** `\d$` — Forces the last character to be a digit.
* **No Special Chars:** `^[a-zA-Z0-9]*$` — Matches strings with only letters and numbers.
* **ASCII Range:** `[\x00-\x7F]` — Matches any standard ASCII character.
* **Valid Filename:** `[^\\/:*?"<>|]` — Excludes characters forbidden in filenames.
* **Capitalized Words:** `[A-Z][a-z]*` — Matches words starting with a capital.
* **Vowel then Digit:** `[aeiou]\d` — Matches a vowel followed by a number.
* **No Spaces:** `^[^\s]*$` — Validates that no spaces exist in the string.
* **Literal Brackets:** `[\[\]]` — Matches [ or ].

---

## 📂 Category 3: Quantifiers & Repetition (31-50)
**Focus:** Controlling length and frequency.



* **Optional Char (color/colour):** `colou?r` — The u is optional.
* **One or More Digits:** `\d+` — Matches a sequence of numbers.
* **Zero or More Spaces:** `word\s*` — Matches "word" followed by any amount of space.
* **Fixed Length:** `^\d{5}$` — Validates exactly 5 digits (Zip Code).
* **Range Length:** `^[a-z0-9_-]{3,16}$` — Username validation (3 to 16 chars).
* **Min Length:** `.{8,}` — Password length check (minimum 8).
* **Greedy Match:** `<div>.*</div>` — Matches everything from the first <div> to the last </div>.
* **Lazy Match:** `<div>.*?</div>` — Matches only until the first closing </div>.
* **Optional Prefix:** `(\+1)?\d{10}` — Matches US numbers with or without +1.
* **Exactly Two:** `([a-z])\1` — Finds double letters (e.g., "aa", "bb").
* **Max Repetitions:** `[aeiou]{1,3}` — Matches between 1 and 3 vowels.
* **Repeating Groups:** `(abc){3}` — Matches "abcabcabc".
* **Phone Format:** `\d{3}-\d{3}-\d{4}` — Matches 000-000-0000.
* **Currency Format:** `\$\d+(\.\d{1,2})?` — Matches $10, $10.5, or $10.99.
* **IP Segment:** `\d{1,3}` — Matches 1 to 3 digits.
* **Binary String:** `^[01]+$` — Matches only 0s and 1s.
* **Multiple Newlines:** `\n{2,}` — Finds double spacing.
* **C-Comments:** `\/\*[\s\S]*?\*\/` — Matches multi-line comments.
* **HTML Tags:** `<[a-z0-9]+>` — Matches standard opening tags.
* **Leading Zeros:** `^0\d+` — Matches numbers starting with zero.

---

## 📂 Category 4: Grouping & Capturing (51-70)
**Focus:** Data extraction and referencing.

* **Email Domain:** `@([a-z0-9.-]+)` — Captures everything after the @.
* **Non-Capturing Protocol:** `(?:http|https|ftp)://` — Groups protocols without saving them.
* **Named Groups (Python):** `(?P<id>\d+)` — Assigns a name to a capture group.
* **Nested Groups:** `((\d{3})-(\d{3}-\d{4}))` — Captures area code and full number separately.
* **OR Logic:** `(Apple|Orange)` — Matches either word.
* **Duplicate Words:** `(\b\w+)\s+\1` — Finds repeated words in a row.
* **Balanced Quotes:** `(['"])(.*?)\1` — Ensures opening and closing quotes match.
* **URL Components:** `(\w+):\/\/([^/]+)` — Captures protocol and domain.
* **Swap Words:** `(\w+)\s(\w+)` — Used for "First Last" to "Last, First" replacement.
* **Strip HTML Tags:** `<[^>]*>` — Matches tags for removal.
* **CSV Column:** `([^,]+)` — Captures data between commas.
* **MAC Address:** `([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})` — Matches standard MAC format.
* **Markdown Bold:** `(\*\*|__)(.*?)\1` — Captures content inside bold markers.
* **Env Variable:** `^(\w+)=(.*)$` — Captures Key and Value.
* **SQL Values:** `'([^']*)'` — Extracts values inside single quotes.
* **Time Format:** `(\d{2}):(\d{2}):(\d{2})` — Captures hours, mins, secs.
* **Log Date:** `\[(\d{4}-\d{2}-\d{2})` — Extracts date from brackets.
* **URL Path:** `site\.com/(.*)` — Captures the URI path.
* **Currency Code:** `(\d+)\s([A-Z]{3})` — Captures amount and currency code.
* **Version Major:** `v(\d+)\.(\d+)` — Captures major and minor version numbers.

---

## 📂 Category 5: Assertions & Lookarounds (71-90)
**Focus:** Zero-width conditions (Advanced logic).



* **Positive Lookahead:** `\w+(?=;)` — Match word only if followed by ;.
* **Negative Lookahead:** `\b\w+\b(?!ing)` — Match word only if NOT followed by "ing".
* **Positive Lookbehind:** `(?<=\$)\d+` — Match digits only if preceded by $.
* **Negative Lookbehind:** `(?<!-)\d+` — Match digits only if NOT preceded by -.
* **Strong Password:** `^(?=.*\d)(?=.*[A-Z]).{8,}$` — Requires 1 digit and 1 uppercase.
* **Between Tags:** `(?<=<title>).*?(?=</title>)` — Extracts content inside title tags.
* **Whole Word Only:** `(?<=\s|^)word(?=\s|$)` — Matches "word" but not "wordy".
* **Exclude Extension:** `^.*(?!\.exe$).*$` — Matches files that are not executables.
* **URL Condition:** `^(?=https).*?\.com$` — Must start with https and end with .com.
* **Decimal Lookahead:** `\d+(?=\.\d{2}$)` — Matches the integer part of a price.
* **Followed by Space:** `\w+(?=\s)` — Matches word without the following space.
* **Preceded by Label:** `(?<=Ref:)\d+` — Matches ID in "Ref:12345".
* **Exclude "admin":** `\b(?!admin\b)\w+\b` — Matches any word except "admin".
* **Symbol & Length:** `^(?=.*[!@#$%^&*])(?=.{8,10}$).*$` — Symbol check + length limit.
* **No Digits Check:** `^(?!\d+$).*$` — Ensures the string isn't just numbers.
* **camelCase Humps:** `[a-z](?=[A-Z])` — Finds the boundary in camelCase.
* **Domain if http:** `(?<=http://)[a-z.]+` — Matches domain only if http is used.
* **Exclude log/txt:** `^.*\.(?!(log|txt)$)[^.]+$` — Match any extension except log or txt.
* **Variable Name:** `^(?!\d)\w+$` — Validates variable names (no leading digit).
* **Price Check:** `\d+(?=\s?USD)` — Matches numbers followed by currency code.

---

## 📂 Category 6: Real World Architecture (91-110)

* **Full Email (Strict):** `[a-z0-9!#$%&'*+/=?^_{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?`
* **IPv4 (Strict):** `^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$`
* **Remove Comments:** `(\/\*[\s\S]*?\*\/|\/\/.+)` — Matches both block and line comments.
* **JSON Key-Value:** `"([^"]+)":\s*"([^"]*)"` — Extracts JSON pairs.
* **YouTube ID:** `(?:v=|\/)([0-9A-Za-z_-]{11}).*` — Extracts 11-char video ID.
* **CSS Colors:** `^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$` — Matches hex color codes.
* **HTML Attributes:** `(\w+)\s*=\s*["']([^"']*)["']` — Extracts key="value" pairs.
* **UUID Validation:** `^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$`
* **Find TODO in Code:** `(?i)\/\/.*TODO.*`
* **Camel to Snake:** `([a-z])([A-Z])` — Replace with `$1_$2`.
* **Leading/Trailing Space:** `^\s+|\s+$`
* **Markdown Links:** `\[([^\]]+)\]\(([^)]+)\)`
* **Extract API Path Params:** `\/:([a-zA-Z_]\w*)`
* **Bearer Token Header:** `^Bearer\s+([A-Za-z0-9-_=]+\.[A-Za-z0-9-_=]+\.?[A-Za-z0-9-_.+/=]*)$`
* **SQL SELECT Statement:** `(?i)SELECT\s+(.+?)\s+FROM\s+([a-zA-Z_]\w*)`
* **Credit Card (Visa):** `^4[0-9]{12}(?:[0-9]{3})?$`
* **Mastercard:** `^5[1-5][0-9]{14}$`
* **Handlebars Templates:** `\{\{([^}]+)\}\}`
* **JWT Token Structure:** `^[A-Za-z0-9-_=]+\.[A-Za-z0-9-_=]+\.?[A-Za-z0-9-_.+/=]*$`
* **Hexadecimal String:** `^[0-9a-fA-F]+$`

---

## 🚀 Optimization Tips

* **Be Specific:** Use `[^"]*` instead of `.*?` inside quotes to reduce backtracking.
* **Atomic Groups:** If your language supports them, use `(?>...)` to prevent the engine from re-trying failed matches.
* **Anchors:** Use `^` and `$` to prevent the engine from starting a search at every character in a large string.