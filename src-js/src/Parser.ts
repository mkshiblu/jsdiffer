import * as babelParser from '@babel/parser';
import * as babelTravese from '@babel/traverse';
import { File as BabelFile, Program } from '@babel/types';
import { CompositeFragment, Container } from './RmTypes';
import * as visitor from './Visitor';

export function parse(content: string) {
  console.time('parse');
  const ast: BabelFile = parseAndMakeAst(content);
  traverse(ast);
  console.timeEnd('parse');
}

function parseAndMakeAst(content: string): BabelFile {
  return babelParser.parse(content, {
    sourceType: 'unambiguous',
    allowImportExportEverywhere: true,
    allowReturnOutsideFunction: true,
    plugins: [
      'jsx',
      'objectRestSpread',
      'exportDefaultFrom',
      'exportNamespaceFrom',
      'classProperties',
      'flow',
      'dynamicImport',
      'decorators-legacy',
      'optionalCatchBinding',
    ],
  });
}

function traverse(ast: BabelFile) {
  //const blockCodeFragment = new CompositeStatement();
  const container = new Container();
  babelTravese.default(
    ast,
    //visitor.containerVisitor,
    visitor.createProgramVisitor(),
    undefined,
    container,
    undefined,
  );

  console.time('JSON.stringify');
  //const result = asJson ? JSON.stringify(blockCodeFragment) : blockCodeFragment;
  console.timeEnd('JSON.stringify');
}
