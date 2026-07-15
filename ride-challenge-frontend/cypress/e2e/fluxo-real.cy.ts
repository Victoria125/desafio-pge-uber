const runReal = Cypress.env('REAL') === '1' || Cypress.env('REAL') === 1 || Cypress.env('REAL') === true;

const suffix = Date.now();
const clientEmail = `cliente.${suffix}@email.com`;
const driverEmail = `motorista.${suffix}@email.com`;
const password = 'secret123';
const startAddress = `Rua do Teste E2E, ${suffix}`;
const destinationAddress = `Avenida Destino E2E, ${suffix}`;

function createAccount(type: 'CLIENT' | 'DRIVER', name: string, email: string): void {
  cy.visit('/login');

  const profileLabel = type === 'CLIENT' ? 'Passageiro' : 'Motorista';
  cy.contains('button', profileLabel).click();
  cy.contains('button', 'Criar conta').click();

  cy.get('#account-name').should('be.visible');
  cy.wait(500);
  cy.get('#account-name').type(name).should('have.value', name);
  cy.get('#account-email').type(email).should('have.value', email);
  cy.get('#account-password').type(password).should('have.value', password);
  cy.contains('button', 'Cadastrar').click();
}

(runReal ? describe : describe.skip)(
  'Fluxo real - cliente cria corrida e motorista aceita',
  { defaultCommandTimeout: 20000 },
  () => {
    it('cadastra um cliente, autentica e cria uma corrida', () => {
      createAccount('CLIENT', 'Cliente E2E', clientEmail);

      cy.url().should('include', '/client/rides');

      cy.contains('button', 'Nova corrida').click();
      cy.get('#start-address').type(startAddress);
      cy.get('#destination-address').type(destinationAddress);
      cy.contains('button', 'Salvar').click();

      cy.contains(startAddress).should('be.visible');
    });

    it('cadastra um motorista, autentica e aceita a corrida criada', () => {
      createAccount('DRIVER', 'Motorista E2E', driverEmail);

      cy.url().should('include', '/driver/rides/available');

      cy.contains('article', startAddress).contains('button', 'Aceitar').click();

      cy.contains('article', startAddress).contains('Em andamento').should('be.visible');
    });

    it('nao autentica com senha errada', () => {
      cy.visit('/login');

      cy.get('#login-email').type(clientEmail);
      cy.get('#login-password').type('senha-errada');
      cy.contains('button', 'Entrar').click();

      cy.url().should('include', '/login');
      cy.contains(startAddress).should('not.exist');
    });
  }
);
