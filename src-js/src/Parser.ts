//import { babelParser } from '';

export function parse(script: string) {
  console.time('parse');
  // const ast = babelParser.parse(script, {
  //   sourceType: 'unambiguous',
  //   allowImportExportEverywhere: true,
  //   allowReturnOutsideFunction: true,
  //   plugins: [
  //     'jsx',
  //     'objectRestSpread',
  //     'exportDefaultFrom',
  //     'exportNamespaceFrom',
  //     'classProperties',
  //     'flow',
  //     'dynamicImport',
  //     'decorators-legacy',
  //     'optionalCatchBinding',
  //   ],
  // });
  console.timeEnd('parse');
}
