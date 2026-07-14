const contas = [
  { id: 'c1', name: 'Maria Silva', email: 'maria@email.com', type: 'CLIENT' },
  { id: 'd1', name: 'Joao Souza', email: 'joao@email.com', type: 'DRIVER' },
];

describe('Smoke - fluxo de entrada', () => {
  beforeEach(() => {
    cy.intercept('GET', '**/accounts', { body: contas }).as('listAccounts');
    cy.intercept('GET', '**/rides', { body: [] }).as('listRides');
  });

  it('redireciona visitante sem sessao para /login', () => {
    cy.visit('/');
    cy.url().should('include', '/login');
  });

  it('renderiza a tela de login e filtra contas por perfil', () => {
    cy.visit('/login');
    cy.wait('@listAccounts');

    cy.contains('Desafio de Corridas').should('be.visible');
    cy.contains('.account-card', 'Maria Silva').should('be.visible');

    cy.contains('button', 'Motorista').click();
    cy.contains('.account-card', 'Joao Souza').should('be.visible');
    cy.contains('.account-card', 'Maria Silva').should('not.exist');
  });

  it('entra como cliente e chega na tela de corridas', () => {
    cy.visit('/login');
    cy.wait('@listAccounts');

    cy.contains('.account-card', 'Maria Silva').click();

    cy.url().should('include', '/client/rides');
    cy.wait('@listRides');
  });
});
