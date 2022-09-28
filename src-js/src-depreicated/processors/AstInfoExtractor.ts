import { NodePath } from '@babel/traverse';

export function extractContainerNamesAndLocation() {}

export function createSourceLocation(path: NodePath) {
  const loc = path.node.loc;
  return {
    start: path.node.start,
    end: path.node.start,
    startLine: loc.start.line,
    startColumn: loc.start.column,
    endLine: loc.end.line,
    endColumn: loc.end.column,
  };
}
