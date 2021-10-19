export interface Fragment {
  type: string;
}

export interface SourceLocation {
  file?: string;
  start: number;
  end: number;
  startLine: number;
  startColumn: number;
  endLine: number;
  endColumn: number;
}

export class CompositeFragment implements Fragment {
  type: 'BlockStatement';
  functionDeclarations: [];
  classDeclarations: [];
  statements: [];
}

export class LeafFragment implements Fragment {
  readonly type: string;
  constructor(type: string) {
    this.type = type;
  }
  functionDeclarations: [];
  classDeclarations: [];
  statements: [];
}

export interface ModelStorageProvider {
  registerClassDeclaration();
  registerFunctionDeclaration();
}

export class Container implements ModelStorageProvider {
  readonly body: ContainerBody = new ContainerBody();

  registerClassDeclaration() {}

  registerFunctionDeclaration() {}

  registerStatement() {}
}

export class ContainerBody {
  functionDeclarations: [];
  classDeclarations: [];
  statements: [];
}
