// Tool adapter for the repository's JSON-compatible YAML subset.
// Preload only with the installed specsmd bolt-complete.cjs.
// Supplies its four fs-extra operations from Node builtins and its metadata
// load/dump from the local subset; no package installation or runtime changes.
const path = require('node:path');
const fs = require('node:fs/promises');
const Module = require('node:module');
const toolPath = path.resolve(process.argv[1]);

function load(text) {
    const result = {};
    let parent;
    for (const line of text.split('\n')) {
        if (!line.trim() || line.trimStart().startsWith('#')) continue;
        const match = /^([ ]*)([A-Za-z_][\w-]*):(?:[ ]*(.*))?$/.exec(line);
        if (!match || ![0, 2].includes(match[1].length)) throw new Error('Unsupported metadata: ' + line);
        const [, indent, key, raw] = match;
        const target = indent ? result[parent] : result;
        if (!target || Object.hasOwn(target, key)) throw new Error('Invalid metadata parent/duplicate: ' + key);
        if (!raw) {
            if (indent) throw new Error('Nested metadata mappings are unsupported');
            target[key] = {}; parent = key;
        } else {
            target[key] = JSON.parse(raw);
            if (!indent) parent = undefined;
        }
    }
    return result;
}

function scalar(value) {
    if (value instanceof Date) return JSON.stringify(value.toISOString().replace(/\.\d{3}Z$/, 'Z'));
    const encoded = JSON.stringify(value);
    if (encoded === undefined) throw new Error('Unsupported undefined metadata value');
    return encoded;
}

function dump(metadata) {
    return Object.entries(metadata).map(([key, value]) => {
        if (value && typeof value === 'object' && !Array.isArray(value) && !(value instanceof Date)) {
            return key + ':\n' + Object.entries(value).map(([nestedKey, nestedValue]) =>
                '  ' + nestedKey + ': ' + scalar(nestedValue)).join('\n');
        }
        return key + ': ' + scalar(value);
    }).join('\n') + '\n';
}

const originalLoad = Module._load;
Module._load = function loadToolDependency(request, parent, isMain) {
    if (parent && parent.filename === toolPath) {
        if (request === 'js-yaml') return { load, dump };
        if (request === 'fs-extra') return {
            readdir: fs.readdir, readFile: fs.readFile, writeFile: fs.writeFile,
            async pathExists(file) {
                try { await fs.access(file); return true; }
                catch (error) {
                    if (error.code === 'ENOENT' || error.code === 'ENOTDIR') return false;
                    throw error;
                }
            }
        };
    }
    return originalLoad.call(this, request, parent, isMain);
};
