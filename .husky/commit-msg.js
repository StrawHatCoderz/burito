const fs = require('fs');

const msgPath = process.argv[2];
if (!msgPath) {
  console.error('No commit message file path provided.');
  process.exit(1);
}

const content = fs.readFileSync(msgPath, 'utf8');
// Filter out comment lines (e.g. git status lines)
const lines = content.split(/\r?\n/).filter(line => !line.trim().startsWith('#'));

// Find first non-empty line
const firstNonEmptyIndex = lines.findIndex(line => line.trim() !== '');
if (firstNonEmptyIndex === -1) {
  console.error('❌ Commit message cannot be empty.');
  process.exit(1);
}

const firstLine = lines[firstNonEmptyIndex].trim();
// Expected format: | story-id | type | subject
const regex = /^\|[ \t]*([^|]+?)[ \t]*\|[ \t]*([^|]+?)[ \t]*\|[ \t]*(.+)$/;
const match = firstLine.match(regex);

if (!match) {
  console.error('❌ Commit message subject line must follow the format:');
  console.error('   | <story-id> | <type> | <subject>');
  console.error('   Example: | BR-701 | chore | adds commit message format hook');
  process.exit(1);
}

const [, storyId, type, subject] = match;

// 1. Validate Story ID (e.g. BR-701)
if (!/^[A-Z]+-[0-9]+$/.test(storyId)) {
  console.error(`❌ Invalid Story ID: "${storyId}". Must be in format <PROJECT>-<NUMBER> (e.g., BR-701).`);
  process.exit(1);
}

// 2. Validate Type
const allowedTypes = ['feat', 'chore', 'refactor', 'fix', 'docs', 'test', 'style', 'ci'];
if (!allowedTypes.includes(type)) {
  console.error(`❌ Invalid type: "${type}". Allowed types: ${allowedTypes.join(', ')}.`);
  process.exit(1);
}

// 3. Validate Subject starts with a verb in third-person present tense (ending in "s")
const words = subject.trim().split(/\s+/);
const firstWord = words[0];
if (!firstWord || !/^[a-z]+s$/.test(firstWord)) {
  console.error(`❌ Subject line first word must be a lowercase verb in third-person present tense (ending in "s", e.g., "adds", "creates", "fixes").`);
  console.error(`   Found: "${firstWord}"`);
  process.exit(1);
}

// 4. Validate subsequent lines (excluding empty lines) must be bullet points starting with '* '
for (let i = firstNonEmptyIndex + 1; i < lines.length; i++) {
  const line = lines[i].trim();
  if (line === '') continue;
  if (!line.startsWith('*')) {
    console.error(`❌ Description line ${i + 1} must be a bullet point starting with "* ".`);
    console.error(`   Found: "${line}"`);
    process.exit(1);
  }
}

console.log('=== ✅ Commit Message Format Passed! ===');
process.exit(0);
