const loginResponse = {
  token: 'jwt-token',
  expiresIn: 86400,
  accountId: 'c1',
  name: 'Maria Silva',
  email: 'maria@email.com',
  type: 'CLIENT',
};

describe('Smoke - fluxo de entrada', () => {
  beforeEach(() => {
    cy.intercept('POST', '**/auth/login', { body: loginResponse }).as('login');
    cy.intercept('GET', '**/rides', { body: [] }).as('listRides');
  });

  it('redireciona visitante sem sessao para /login', () => {
    cy.visit('/');
    cy.url().should('include', '/login');
  });

  it('renderiza a tela de login com email, senha e cadastro', () => {
    cy.visit('/login');

    cy.contains('Desafio de Corridas').should('be.visible');
    cy.get('#login-email').should('be.visible');
    cy.get('#login-password').should('be.visible');
    cy.contains('button', 'Criar conta').should('be.visible');
  });

  it('entra como cliente e chega na tela de corridas', () => {
    cy.visit('/login');

    cy.get('#login-email').type('maria@email.com');
    cy.get('#login-password').type('secret123');
    cy.contains('button', 'Entrar').click();

    cy.wait('@login');
    cy.url().should('include', '/client/rides');
    cy.wait('@listRides');
  });
});
