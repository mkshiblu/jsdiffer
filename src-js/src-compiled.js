import { babelParser } from '@babel/parser';
export function parse(script) {
  console.time('parse');
  const ast = babelParser.parse(script, {
    sourceType: 'unambiguous',
    allowImportExportEverywhere: true,
    allowReturnOutsideFunction: true,
    plugins: ['jsx', 'objectRestSpread', 'exportDefaultFrom', 'exportNamespaceFrom', 'classProperties', 'flow', 'dynamicImport', 'decorators-legacy', 'optionalCatchBinding']
  });
  console.timeEnd('parse');
}
export default function visit(path, parentFragment, container) {
  switch (path.node.type) {}
}
