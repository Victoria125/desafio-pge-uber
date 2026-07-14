# Apresentacao explicada do projeto Ride Challenge

Este documento foi refeito em formato de explicacao corrida. A proposta desta versao e evitar topicos soltos e transformar o conteudo em uma fala detalhada, como se voce estivesse explicando o projeto para uma pessoa que esta comecando agora e, aos poucos, levando essa pessoa ate um nivel mais avancado de entendimento. Os capitulos continuam existindo para organizar o estudo, mas dentro de cada capitulo a explicacao foi escrita em paragrafos, com começo, meio e fim.

Voce pode usar este material de duas formas. A primeira forma e estudar lendo em ordem, porque ele começa explicando o que o sistema faz e depois entra nas tecnologias, nos servicos, nos arquivos e nos fluxos. A segunda forma e usar como roteiro de apresentacao, porque muitas partes ja estao escritas como frases que voce pode adaptar para falar diante da banca.

O projeto Ride Challenge e uma aplicacao full stack que simula um fluxo simplificado de corridas parecido com um aplicativo de transporte. Existe uma parte visual, feita em Angular, onde o usuario entra como cliente ou motorista. Existe uma parte de backend, feita em Java com Spring Boot e Spring Cloud, onde ficam as regras de negocio, as APIs, os microsservicos e as integracoes com banco, fila, cache e tempo real. O cliente cria uma corrida informando origem e destino. O motorista visualiza corridas disponiveis em tempo real e pode aceitar uma corrida. Quando isso acontece, o status muda, o dado e salvo e a tela e atualizada.

Uma forma segura de abrir a apresentacao e dizer que este projeto nao e apenas um cadastro simples. Ele foi construido para demonstrar integracao entre varias partes de um sistema moderno. O Angular cuida da interface. O API Gateway centraliza a entrada. O Eureka ajuda os servicos a se encontrarem. O Config Server centraliza configuracoes. O Account Service cuida das contas. O Ride Service cuida das corridas. O PostgreSQL salva os dados. O Kafka transporta eventos. O Redis guarda status em cache. O WebSocket atualiza a tela do motorista em tempo real. O Docker Compose sobe tudo junto para facilitar a execucao e a demonstracao.

[PAGEBREAK]

## Capitulo 1 - O que o projeto faz na pratica

Para explicar o projeto desde o nivel iniciante, comece contando a historia do usuario. Imagine que uma pessoa abre a aplicacao no navegador. Ela escolhe se quer entrar como cliente ou como motorista. Se entrar como cliente, consegue criar uma corrida informando uma origem e um destino. Essa solicitacao sai do navegador, passa pelo frontend Angular, chega ao backend pelo API Gateway e e processada pelo Ride Service. O Ride Service valida se o cliente existe, cria a corrida, salva no banco de dados e publica um evento no Kafka dizendo que uma nova corrida foi criada.

Depois que esse evento e publicado, outra parte do Ride Service escuta a fila Kafka. Quando a mensagem chega, o sistema envia uma notificacao por WebSocket para os motoristas conectados. Isso significa que a tela do motorista nao precisa ficar atualizando manualmente o tempo todo. Quando uma nova corrida aparece, o servidor avisa o navegador. Esse comportamento e importante porque aproxima o projeto de um sistema em tempo real.

Quando o motorista aceita uma corrida, acontece outro fluxo. O frontend do motorista envia uma requisicao para o backend dizendo qual corrida ele quer aceitar e qual e o id do motorista. O Ride Service consulta o Account Service para confirmar se aquele usuario realmente e motorista. Depois busca a corrida no banco e verifica se ela ainda esta disponivel. Se estiver, atribui o motorista, muda o status para em andamento, salva no PostgreSQL, grava o status no Redis e envia uma notificacao para atualizar as telas.

No nivel iniciante, voce pode resumir dizendo que o projeto permite criar usuarios, criar corridas, mostrar corridas para motoristas e aceitar uma corrida. No nivel intermediario, voce explica que esses passos passam por frontend, gateway, microsservicos, banco, Kafka, Redis e WebSocket. No nivel avancado, voce mostra que o projeto foi separado em camadas para reduzir acoplamento, facilitar testes e deixar cada tecnologia com uma responsabilidade clara.

A principal mensagem deste capitulo e que cada parte do projeto existe para resolver um problema especifico. O frontend resolve a interacao com o usuario. A API resolve a comunicacao entre navegador e servidor. O banco resolve a persistencia. A fila resolve comunicacao assincrona. O cache melhora consulta de status. O WebSocket resolve tempo real. Os testes ajudam a garantir que as regras funcionem.

[PAGEBREAK]

## Capitulo 2 - Como uma requisicao percorre o sistema

Para entender o projeto, e muito importante entender o caminho de uma requisicao. Uma requisicao e um pedido que o navegador faz para o backend. Quando o usuario clica em um botao no Angular, como criar conta ou criar corrida, o componente da tela chama um service do frontend. Esse service usa o HttpClient do Angular para enviar uma chamada HTTP para o endereco base da API, que no projeto e http://localhost:8080.

Esse endereco nao aponta diretamente para o Account Service nem diretamente para o Ride Service. Ele aponta para o API Gateway. O Gateway funciona como a porta principal de entrada. Quando a chamada chega em uma rota que comeca com /accounts, o Gateway encaminha para o Account Service. Quando a chamada chega em uma rota que comeca com /rides, o Gateway encaminha para o Ride Service. Quando a chamada e de WebSocket em /ws, o Gateway tambem encaminha para o Ride Service, mas usando a rota apropriada para WebSocket.

O Gateway nao precisa decorar um IP fixo de cada servico. Ele usa Eureka para descobrir onde os servicos estao. Isso e um conceito importante em microsservicos. Em vez de um servico depender de endereco fixo, cada servico se registra em um catalogo, e os outros servicos conseguem localiza-lo pelo nome. No projeto, os nomes importantes sao ACCOUNT-SERVICE e RIDE-SERVICE.

Depois que a requisicao chega ao servico correto, o controller recebe os dados HTTP e transforma isso em um comando de aplicacao. Por exemplo, uma chamada para criar corrida chega em RideController, que monta um CreateRideCommand. O command e passado para o caso de uso DefaultCreateRideUseCase. O caso de uso executa a regra de negocio, conversa com contratos do dominio e devolve um output. Depois o controller transforma esse output em uma response JSON.

Essa sequencia mostra uma decisao arquitetural importante. O controller nao deve conter a regra principal. Ele e uma camada de entrada. A regra principal fica no caso de uso e na entidade de dominio. Isso deixa o codigo mais organizado e facilita testes, porque e possivel testar o caso de uso sem subir servidor HTTP.

[PAGEBREAK]

## Capitulo 3 - A arquitetura do backend

O backend fica dentro da pasta ride-challenge-backend. Ele foi organizado como um conjunto de microsservicos. Um microsservico e uma aplicacao menor, com uma responsabilidade especifica, que trabalha junto com outras aplicacoes. Em vez de colocar tudo em um unico backend grande, o projeto separa configuracao, descoberta de servicos, entrada da API, contas e corridas.

O Config Server e o servico que centraliza configuracoes. Ele roda na porta 8888 e entrega arquivos YAML para os outros servicos. Isso permite que as configuracoes fiquem organizadas em um lugar so. O Eureka Server e o servico de descoberta. Ele roda na porta 8761 e funciona como um catalogo onde os outros servicos se registram. O API Gateway roda na porta 8080 e e a entrada externa do sistema. O Account Service cuida de contas de clientes e motoristas. O Ride Service cuida do fluxo principal de corridas.

Alem dos servicos Java, o ambiente tem PostgreSQL, Kafka, Redis e frontend. O PostgreSQL e o banco relacional que salva contas e corridas. O Kafka e a fila de mensagens usada quando uma corrida e criada. O Redis e o cache usado para status de corrida. O frontend Angular e servido em localhost:4200 quando o projeto roda via Docker Compose.

Essa arquitetura permite mostrar varias habilidades ao mesmo tempo. Ela mostra que voce sabe construir APIs REST, sabe separar responsabilidades, sabe trabalhar com banco de dados, sabe usar comunicacao assincrona, sabe usar cache, sabe entregar tempo real e sabe empacotar tudo com Docker. Ao apresentar, deixe claro que essa complexidade foi usada para fins didaticos e para aproximar o desafio de uma arquitetura real. Em um sistema pequeno, algumas partes poderiam ser mais simples, mas no contexto do projeto cada tecnologia demonstra um conceito importante.

No nivel avancado, o ponto mais forte da arquitetura e a separacao entre regra de negocio e infraestrutura. As entidades e casos de uso nao ficam presos diretamente ao PostgreSQL, Kafka, Redis ou WebSocket. Eles dependem de interfaces, como RideGateway, AccountClient, SentEventService, RideStatusCache e DriverNotifier. As implementacoes concretas ficam na infraestrutura. Isso e uma forma de reduzir acoplamento e deixar o sistema mais testavel.

[PAGEBREAK]

## Capitulo 4 - Java 17 explicado no projeto

Java 17 e a linguagem usada em todo o backend. Em termos simples, Java e responsavel pela parte que roda no servidor. E no codigo Java que o sistema recebe pedidos do frontend, valida informacoes, aplica regras de negocio, salva dados e integra com outras tecnologias. A escolha de Java faz sentido porque ele e muito usado em sistemas corporativos, tem uma comunidade grande, possui ferramentas maduras e combina muito bem com Spring Boot.

No projeto, Java aparece em classes, interfaces, enums e records. Uma classe representa um objeto ou uma parte do sistema com comportamento. A classe Account representa uma conta. A classe Ride representa uma corrida. Uma interface representa um contrato, ou seja, ela diz o que precisa existir, mas nao diz exatamente como sera feito. RideGateway e uma interface que diz que deve ser possivel salvar e buscar corridas. A implementacao concreta desse contrato e RidePostgresGateway, que usa PostgreSQL.

Os enums aparecem quando o sistema precisa limitar valores aceitos. AccountType define os tipos CLIENT e DRIVER. RideStatus define os estados CREATED, IN_PROGRESS, COMPLETED e CANCELLED. Isso evita que o sistema aceite textos aleatorios como status ou tipo de conta. Em vez de depender de strings soltas, o codigo trabalha com valores controlados.

Os records sao usados para objetos simples de entrada e saida. CreateRideCommand carrega os dados necessarios para criar corrida. CreateRideOutput carrega o id retornado. RideCreatedMessage carrega a mensagem publicada no Kafka. O record e util porque reduz codigo repetitivo, ja que o Java gera automaticamente construtor, acessores e outros metodos basicos.

No nivel avancado, Java ajuda na organizacao por contratos. O dominio define o que precisa atraves de interfaces, e a infraestrutura implementa. Isso facilita testes, porque em um teste unitario voce pode trocar o banco real por um mock. Tambem facilita manutencao, porque se um dia o projeto trocasse PostgreSQL por outro banco, a regra principal poderia continuar dependendo de RideGateway.

[PAGEBREAK]

## Capitulo 5 - Spring Boot explicado no projeto

Spring Boot e o framework que facilita criar os microsservicos Java. Sem Spring Boot, seria necessario configurar manualmente servidor web, rotas HTTP, injecao de dependencias, validacao, conexao com banco, integracao com Kafka, Redis e WebSocket. O Spring Boot entrega uma base pronta e permite que o desenvolvimento foque mais nas regras do projeto.

Os arquivos AccountServiceApplication, RideServiceApplication, ConfigServerApplication, EurekaServerApplication e ApiGatewayApplication sao pontos de entrada dos servicos. Eles normalmente possuem a anotacao SpringBootApplication e um metodo main. O metodo main chama SpringApplication.run, que inicializa a aplicacao, carrega configuracoes, cria objetos gerenciados e sobe o servidor.

As anotacoes do Spring aparecem em varias partes. RestController indica que uma classe recebe chamadas HTTP. Service indica que uma classe e um servico gerenciado pelo Spring. Component registra uma classe generica no contexto. Configuration indica uma classe de configuracao. Bean indica que um metodo cria um objeto que o Spring deve administrar. Essas anotacoes evitam a criacao manual de objetos e permitem que o Spring resolva dependencias automaticamente.

Um conceito central e injecao de dependencias. Em vez de uma classe criar tudo que precisa com new, ela declara no construtor suas dependencias. Por exemplo, DefaultCreateRideUseCase precisa de RideGateway, AccountClient e SentEventService. Ele nao cria essas dependencias diretamente. O Spring monta o grafo de objetos e injeta a implementacao correta. Isso deixa o codigo mais flexivel e testavel.

Na apresentacao, voce pode dizer que Spring Boot foi escolhido porque acelera o desenvolvimento de APIs e microsservicos, integra facilmente com outras tecnologias e permite separar responsabilidades usando controllers, services, repositories, configuracoes e beans.

[PAGEBREAK]

## Capitulo 6 - Maven e os arquivos pom.xml

Maven e a ferramenta que gerencia o backend Java. Ele baixa dependencias, compila o codigo, roda testes e empacota as aplicacoes. O arquivo pom.xml e o arquivo onde cada projeto declara seu nome, versao, dependencias e plugins de build. Pense no pom.xml como a ficha tecnica de cada microsservico.

Na raiz do backend existe um pom.xml com packaging pom. Isso significa que ele nao gera uma aplicacao sozinho. Ele organiza os modulos config-server, eureka-server, api-gateway, account-service e ride-service. Cada modulo tem seu proprio pom.xml, porque cada servico tem dependencias especificas. O Account Service precisa de web, JPA, validacao, Config Client, Eureka Client, PostgreSQL e testes. O Ride Service precisa dessas dependencias e tambem de OpenFeign, Kafka, Redis e WebSocket, porque ele tem mais integracoes.

O Maven tambem organiza os testes. Testes unitarios rodam na fase test. Testes de integracao rodam na fase integration-test com o plugin Failsafe. A fase verify executa o build completo e valida o gate de cobertura. O JaCoCo mede cobertura de testes e foi configurado para exigir pelo menos noventa por cento de cobertura em dominio e aplicacao. Essa decisao mostra preocupacao com qualidade justamente onde ficam as regras mais importantes.

Na apresentacao, explique que Maven evita instalar bibliotecas manualmente. Quando o projeto declara uma dependencia, o Maven baixa a versao correta e monta o classpath. Isso padroniza o ambiente de build e facilita rodar o projeto em outra maquina ou no GitHub Actions.

O ponto avancado e que cada servico declara apenas o que precisa. Isso evita dependencias desnecessarias. O Account Service nao precisa de Kafka porque ele nao publica nem consome eventos. O Ride Service precisa, porque cria corrida e dispara notificacoes baseadas em evento. Esse detalhe mostra que a divisao por servicos tambem aparece no gerenciamento de dependencias.

[PAGEBREAK]

## Capitulo 7 - Config Server explicado

O Config Server e um microsservico responsavel por centralizar configuracoes. Em projetos com varios servicos, cada aplicacao precisa saber porta, nome, banco, endereco do Eureka, endereco de Kafka, Redis e outras informacoes. Se cada servico guardasse tudo isolado, ficaria mais dificil manter. O Config Server resolve isso oferecendo configuracoes a partir de um lugar central.

No arquivo ConfigServerApplication.java, a anotacao EnableConfigServer transforma aquela aplicacao em um servidor de configuracao. O application.yaml do proprio Config Server define que ele roda na porta 8888, usa perfil native e procura arquivos de configuracao dentro de classpath:/configurations. Isso significa que os YAMLs dos servicos ficam empacotados dentro do proprio Config Server.

Dentro da pasta configurations existem arquivos como account-service.yaml, ride-service.yaml, api-gateway.yaml e eureka-server.yaml. O account-service.yaml define a porta 8081, a conexao com o banco account_db e o endereco do Eureka. O ride-service.yaml define a porta 8082, a conexao com ride_db, Redis, Kafka e configuracoes de timeout. O api-gateway.yaml define rotas e CORS. O eureka-server.yaml define a porta 8761 e informa que o proprio Eureka nao precisa se registrar em si mesmo.

Tambem existem arquivos com sufixo docker. Eles ajustam enderecos quando os servicos rodam dentro de containers. Fora do Docker, e comum usar localhost. Dentro do Docker, um container nao acessa outro usando localhost, porque localhost seria ele mesmo. Por isso, no perfil Docker, o Ride Service usa nomes como postgres, redis e kafka. Esse detalhe e importante para demonstrar que voce entende diferenca entre ambiente local e ambiente em containers.

Na apresentacao, diga que o Config Server deixa a configuracao fora do codigo Java. Assim, alterar endereco de banco, porta ou broker nao exige mudar a regra de negocio. Isso e uma pratica comum em sistemas distribuidos.

[PAGEBREAK]

## Capitulo 8 - Eureka Server explicado

Eureka Server e usado para service discovery, que significa descoberta de servicos. Em uma arquitetura de microsservicos, os servicos precisam se encontrar. O API Gateway precisa encontrar o Account Service e o Ride Service. O Ride Service precisa chamar o Account Service. Em vez de depender de IP fixo, os servicos se registram no Eureka e podem ser localizados pelo nome.

O arquivo EurekaServerApplication.java e a classe principal do servico. A anotacao EnableEurekaServer habilita o comportamento de servidor Eureka. O arquivo eureka-server.yaml define que ele roda na porta 8761. Tambem define register-with-eureka como false e fetch-registry como false, porque o proprio servidor Eureka nao precisa se registrar nele mesmo nem buscar uma lista de outros servicos para funcionar como servidor.

Os servicos clientes, como Account Service, Ride Service e API Gateway, apontam para o Eureka usando a propriedade eureka.client.service-url.defaultZone. Quando sobem, eles se registram e passam a aparecer no catalogo. O Gateway entao consegue usar rotas como lb://ACCOUNT-SERVICE e lb://RIDE-SERVICE. O prefixo lb indica que a chamada sera resolvida com balanceamento e descoberta de servico.

Mesmo que no projeto exista apenas uma instancia de cada servico, a arquitetura ja mostra um conceito usado em ambientes maiores. Se existissem varias instancias do Ride Service, o mecanismo de descoberta permitiria distribuir chamadas entre elas. Para a apresentacao, voce pode explicar que Eureka reduz dependencias de endereco fixo e torna a comunicacao entre microsservicos mais flexivel.

O ponto principal e que Eureka nao processa regra de corrida nem salva dados. Ele existe para ajudar os servicos a se encontrarem. Essa e a responsabilidade dele dentro da arquitetura.

[PAGEBREAK]

## Capitulo 9 - API Gateway explicado

O API Gateway e a entrada unica da API. O frontend Angular nao chama diretamente Account Service na porta 8081 nem Ride Service na porta 8082. Ele chama http://localhost:8080, que e o Gateway. O Gateway olha o caminho da requisicao e decide para onde enviar. Chamadas para /accounts vao para o Account Service. Chamadas para /rides vao para o Ride Service. Chamadas para /ws vao para o WebSocket do Ride Service.

O arquivo ApiGatewayApplication.java e pequeno, porque a maior parte da configuracao esta no YAML do Gateway. No api-gateway.yaml aparecem as rotas. A rota account-route usa uri lb://ACCOUNT-SERVICE e predicate Path=/accounts/**. Isso significa que qualquer requisicao cujo caminho comece com /accounts deve ser encaminhada para o Account Service. A rota ride-route faz o mesmo para /rides/**. A rota ride-ws-route encaminha WebSocket para o Ride Service.

O Gateway tambem configura CORS. CORS e uma regra de seguranca do navegador que controla quais origens podem chamar a API. Como o frontend roda em http://localhost:4200 e o backend em http://localhost:8080, o navegador considera origens diferentes. Por isso, o Gateway permite a origem do frontend, os metodos HTTP principais e headers.

No nivel iniciante, explique que o Gateway e como uma recepcao. O usuario chega por uma porta unica e a recepcao direciona para o setor correto. No nivel intermediario, explique que ele roteia caminhos para microsservicos usando Eureka. No nivel avancado, explique que ele tambem centraliza preocupacoes transversais, como CORS, logs, seguranca e roteamento, mesmo que nem todas estejam completamente implementadas neste desafio.

Na demonstracao, quando voce mostrar que o frontend usa http://localhost:8080, destaque que esse e o Gateway. Isso ajuda a banca a entender que o navegador nao esta acoplado aos servicos internos.

[PAGEBREAK]

## Capitulo 10 - Account Service explicado como modulo

O Account Service e o microsservico responsavel por contas. Ele permite criar uma conta, listar contas e buscar uma conta por id. No dominio do projeto, uma conta pode ser de cliente ou de motorista. O cliente cria corridas. O motorista aceita corridas. Por isso, o Account Service e consultado pelo Ride Service sempre que precisa validar se um usuario existe ou se uma conta e realmente de motorista.

O arquivo AccountServiceApplication.java inicia o servico. Ele nao tem regra de negocio. Isso e proposital. Em uma aplicacao bem organizada, a classe principal deve apenas iniciar o Spring. A regra fica em outros pacotes. O pacote domain contem a entidade Account, o enum AccountType e a interface AccountGateway. O pacote application contem os casos de uso de criar, buscar e listar. O pacote infrastructure contem controller, models, presenter, entidade JPA, repository, gateway PostgreSQL e tratamento de erros.

A entidade Account representa uma conta no dominio. Ela tem id, nome, email, tipo e data de criacao. O metodo newAccount cria uma conta nova. Ele valida nome, email e tipo, gera um UUID e registra a data atual. O metodo with reconstrui uma conta existente, normalmente quando ela vem do banco. Essa diferenca e importante. newAccount representa criacao nova. with representa remontar algo que ja existe.

O enum AccountType limita os tipos aceitos. O sistema nao aceita qualquer texto. Ele aceita CLIENT ou DRIVER. Essa escolha evita erros de digitacao e deixa a regra mais clara. A interface AccountGateway define o que o dominio precisa em relacao a persistencia. Ela permite salvar, buscar por id e listar. O dominio nao precisa saber que a implementacao usa PostgreSQL.

No nivel avancado, o Account Service demonstra uma estrutura limpa e simples. Ele separa dominio, caso de uso e infraestrutura. Isso faz com que a regra de criacao de conta possa ser testada sem subir banco, servidor HTTP ou Docker.

[PAGEBREAK]

## Capitulo 11 - Account Service por dentro dos casos de uso

O caso de uso de criacao de conta comeca em CreateAccountCommand. Esse record carrega nome, email e tipo. Ele representa o pedido de criacao vindo de fora. O CreateAccountOutput carrega o resultado do caso de uso, que nesse caso e o id criado. CreateAccountUseCase define o contrato abstrato com o metodo execute. DefaultCreateAccountUseCase implementa esse contrato.

Quando DefaultCreateAccountUseCase executa, ele cria uma entidade Account usando Account.newAccount. Isso significa que a validacao principal acontece dentro da entidade, e nao espalhada pelo caso de uso. Depois, o caso de uso chama accountGateway.save e retorna o id salvo. Essa sequencia e simples, mas mostra bem a separacao de responsabilidade. A entidade valida e representa a regra. O caso de uso coordena a acao. O gateway persiste.

O caso de uso de busca por id segue outra logica. DefaultGetAccountByIdUseCase chama accountGateway.getById. Como a busca pode nao encontrar nada, o gateway retorna Optional. Se nao existir conta, o caso de uso lanca NoSuchElementException. Mais tarde, o GlobalException transforma isso em uma resposta HTTP 404. Isso e melhor do que retornar null, porque null poderia causar erro inesperado.

O caso de uso de listagem chama accountGateway.getAll e transforma cada Account em ListAccountsOutput. Essa transformacao evita que a API exponha diretamente a entidade do dominio. Mesmo quando os campos sao parecidos, separar output de entidade e uma pratica que protege o dominio de mudancas externas.

Para explicar na apresentacao, diga que casos de uso representam acoes do sistema. Criar conta, buscar conta e listar contas sao casos de uso. Eles nao conhecem HTTP nem banco diretamente. Eles conhecem comandos, outputs e gateways. Isso permite testar essas acoes isoladamente.

[PAGEBREAK]

## Capitulo 12 - Account Service na API e no banco

A API do Account Service e declarada em AccountAPI.java. Esse arquivo define os endpoints HTTP. O POST /accounts cria conta. O GET /accounts/{id} busca uma conta especifica. O GET /accounts lista todas as contas. A interface usa anotacoes como RequestMapping, PostMapping, GetMapping, RequestBody, PathVariable e Valid. Essas anotacoes dizem ao Spring como transformar uma chamada HTTP em uma chamada de metodo Java.

O AccountController implementa AccountAPI. Quando chega uma chamada para criar conta, o controller recebe CreateAccountRequest, cria CreateAccountCommand, executa o caso de uso e devolve CreateAccountResponse com status 201 Created. Quando chega uma chamada para buscar por id, o controller chama GetAccountByIdUseCase e usa AccountPresenter para transformar o output em AccountResponse. Quando chega uma chamada para listar, ele chama ListAccountsUseCase e transforma a lista de outputs em responses.

Os models representam os formatos JSON da API. CreateAccountRequest representa o corpo que vem do frontend. CreateAccountResponse representa a resposta depois de criar conta. AccountResponse e ListAccountsResponse representam os dados que voltam em consultas. Separar models da entidade de dominio evita que detalhes internos vazem para a camada HTTP.

No banco, AccountEntity e a classe JPA. Ela tem anotacoes Entity e Table para mapear a tabela tb_accounts. Cada campo tem Column com nome de coluna. O campo type usa Enumerated com EnumType.STRING, o que faz CLIENT ou DRIVER serem salvos como texto. AccountRepository estende JpaRepository, entao ganha metodos prontos como save, findById e findAll.

AccountPostgresGateway implementa AccountGateway. Ao salvar, ele converte Account para AccountEntity. Ao buscar, converte AccountEntity para Account. Essa conversao parece trabalhosa no comeco, mas e importante para manter o dominio separado da persistencia. Na apresentacao, diga que AccountEntity e banco, Account e regra de negocio. Elas se parecem, mas nao tem a mesma responsabilidade.

[PAGEBREAK]

## Capitulo 13 - Tratamento de erros no Account Service

O tratamento de erros do Account Service fica em GlobalException.java. Esse arquivo usa RestControllerAdvice, que permite capturar excecoes lancadas pelos controllers e casos de uso e transformar em respostas HTTP padronizadas. Sem esse tratamento, cada controller precisaria repetir try e catch, deixando o codigo mais poluido.

Quando ocorre erro de validacao em campos anotados com validacao, o Spring lanca MethodArgumentNotValidException. O GlobalException percorre os erros de campo e monta um mapa com nome do campo e mensagem. A resposta recebe status 400 Bad Request, porque o problema veio nos dados enviados pelo cliente.

Quando o corpo da requisicao esta malformado ou contem valor invalido para enum, pode ocorrer HttpMessageNotReadableException. O sistema responde 400 com uma mensagem dizendo que o body esta invalido. Quando a propria regra de negocio lanca IllegalArgumentException, isso tambem vira 400, porque representa argumento invalido. Quando a conta nao existe e o caso de uso lanca NoSuchElementException, a resposta vira 404 Not Found. Para RuntimeException generica, a resposta vira 500 Internal Server Error.

CustomErrorResponse padroniza o corpo do erro. Em vez de cada erro voltar de um jeito diferente, a API devolve uma estrutura com um mapa de mensagens. Isso facilita o frontend, porque ele pode tentar extrair mensagens desse formato.

Na apresentacao, explique que tratar erros globalmente ajuda a manter controllers limpos e respostas previsiveis. Tambem ajuda o usuario, porque em vez de ver erro tecnico ou stack trace, ele recebe uma mensagem mais controlada.

No nivel avancado, destaque que a API diferencia erro do cliente e erro do servidor. Dados invalidos viram 400. Recurso inexistente vira 404. Falha inesperada vira 500. Essa diferenca e importante para debugar e para criar uma boa experiencia no frontend.

[PAGEBREAK]

## Capitulo 14 - Ride Service explicado como modulo principal

O Ride Service e o modulo mais importante do projeto, porque concentra o fluxo de corridas. Ele cria corridas, edita origem e destino, lista corridas, busca por id, consulta status, aceita corrida, cancela corridas antigas por timeout, publica evento Kafka, consome evento e envia notificacao WebSocket.

O arquivo RideServiceApplication.java inicia o servico. Alem de SpringBootApplication, ele tem EnableFeignClients e EnableScheduling. EnableFeignClients habilita o uso de OpenFeign para chamar o Account Service. EnableScheduling habilita jobs agendados, como o job que cancela corridas antigas. Esse arquivo mostra que o Ride Service precisa de comunicacao entre servicos e de tarefas automaticas em segundo plano.

O pacote domain contem a entidade Ride, o enum RideStatus e contratos como RideGateway, AccountClient, SentEventService, RideStatusCache e DriverNotifier. O pacote application contem os casos de uso de criar, aceitar, atualizar, listar, buscar, consultar status, mudar status e aplicar timeout. O pacote infrastructure contem API REST, persistencia JPA, Redis, Kafka, WebSocket, Feign, job e configuracao de beans.

Quando voce explicar esse modulo, destaque que ele orquestra varias tecnologias, mas a regra principal continua organizada. O caso de uso nao escreve SQL, nao manipula socket diretamente e nao chama KafkaTemplate diretamente. Ele chama contratos. As implementacoes ficam na infraestrutura.

No nivel iniciante, diga que o Ride Service e o servico das corridas. No nivel intermediario, diga que ele coordena conta, banco, fila, cache e tempo real. No nivel avancado, diga que ele usa arquitetura em camadas e inversao de dependencia para manter a regra de negocio independente dos detalhes externos.

[PAGEBREAK]

## Capitulo 15 - A entidade Ride explicada com cuidado

Ride.java representa uma corrida dentro do dominio. Ela tem id, userId, driverId, startAddress, destinationAddress, status, createdAt e updatedAt. O id identifica a corrida. O userId identifica o cliente que criou. O driverId identifica o motorista que aceitou, mas pode ser nulo enquanto a corrida esta disponivel. startAddress e destinationAddress guardam origem e destino. status mostra o estado da corrida. createdAt e updatedAt registram datas.

O metodo newRide cria uma corrida nova. Ele valida userId, origem e destino. Depois gera UUID, pega o horario atual com Instant.now e cria uma corrida com driverId nulo e status CREATED. Essa regra e muito importante. Toda corrida nova nasce sem motorista e aguardando aceite. Isso representa o estado inicial do fluxo.

O metodo with reconstrui uma corrida existente. Ele e usado quando o sistema le dados do banco e precisa transformar RideEntity em Ride. Ele nao gera id novo nem data nova, porque esta reconstruindo um objeto que ja existia. Essa distincao ajuda a evitar confusao entre criacao e leitura.

O metodo assignDriver representa o aceite da corrida. Ele valida se o driverId nao e nulo nem vazio. Depois atribui o motorista, muda o status para IN_PROGRESS e atualiza updatedAt. Isso mostra que aceitar uma corrida nao e apenas preencher um campo. E uma transicao de estado.

O metodo updateRoute permite alterar origem e destino. Antes de alterar, ele verifica se a corrida esta COMPLETED ou CANCELLED. Se estiver, lanca erro, porque uma corrida finalizada ou cancelada nao deve ser editada. Depois valida os novos enderecos, altera os campos e atualiza updatedAt.

O metodo changeStatus muda o status e atualiza a data. Ele e usado em fluxos como timeout. Os metodos validate e validateRoute garantem que dados obrigatorios nao sejam nulos nem vazios. No nivel avancado, diga que Ride e uma entidade rica, porque contem comportamento e regras. Ela nao e apenas um conjunto de getters.

[PAGEBREAK]

## Capitulo 16 - O ciclo de vida da corrida

O ciclo de vida da corrida e controlado pelo enum RideStatus. Uma corrida com status CREATED foi criada e ainda esta aguardando motorista. Uma corrida com status IN_PROGRESS foi aceita e esta em andamento. Uma corrida com status COMPLETED foi concluida. Uma corrida com status CANCELLED foi cancelada.

Mesmo que o projeto nao tenha uma tela completa para concluir corrida, o status COMPLETED existe para representar um estado final possivel. Isso mostra que o modelo foi pensado como um fluxo de corrida, nao apenas como uma tabela simples. O status CANCELLED aparece no timeout, quando uma corrida fica tempo demais sem ser aceita.

Quando o cliente cria corrida, o status inicial e CREATED. Quando o motorista aceita, assignDriver muda para IN_PROGRESS. Quando o job de timeout encontra uma corrida antiga ainda em CREATED, ele muda para CANCELLED. O metodo updateRoute bloqueia edicao quando a corrida esta COMPLETED ou CANCELLED.

Na apresentacao, explique que status e uma forma de controlar regras. Sem status, o sistema nao saberia se uma corrida pode ser aceita, editada ou cancelada. O status tambem ajuda o frontend a mostrar mensagens diferentes para cliente e motorista. Para o cliente, CREATED aparece como "Aguardando motorista". Para o motorista, CREATED aparece como "Disponivel". A regra e a mesma, mas a linguagem muda conforme a perspectiva.

No nivel avancado, voce pode dizer que o sistema poderia evoluir para uma maquina de estados mais formal. Por enquanto, as transicoes principais estao nos metodos da entidade e nos casos de uso. Isso e suficiente para o desafio e deixa o comportamento claro.

[PAGEBREAK]

## Capitulo 17 - Contratos do dominio no Ride Service

O Ride Service define varias interfaces no dominio. Essas interfaces existem porque a regra de negocio precisa de algumas capacidades, mas nao deve depender diretamente da tecnologia que implementa essas capacidades. RideGateway e o contrato de persistencia. Ele permite salvar, buscar por id, listar todas e buscar corridas criadas antes de uma data. A implementacao concreta e RidePostgresGateway.

AccountClient e o contrato para consultar contas. O Ride Service precisa saber se um cliente ou motorista existe, mas nao deve conhecer detalhes internos do Account Service. A implementacao concreta usa OpenFeign. AccountData e um record simples com os dados de conta que o Ride Service precisa: id, nome e tipo.

SentEventService e o contrato para enviar evento. Na implementacao atual, o evento vai para Kafka. Mas o caso de uso de criacao nao precisa conhecer KafkaTemplate. Ele sabe apenas que precisa enviar um evento de corrida criada. DriverNotifier e o contrato para notificar motoristas. A implementacao atual usa WebSocket, mas a regra de negocio depende apenas do contrato.

RideStatusCache e o contrato para cache de status. A implementacao atual usa Redis. RideStatusData representa o dado retornado pelo cache. RideNotification representa a mensagem enviada para motoristas, contendo dados da corrida e status.

Essa organizacao permite explicar um conceito avancado de forma simples. A regra de negocio diz o que precisa acontecer. A infraestrutura decide como isso acontece. O dominio pede "salve a corrida", e o PostgreSQL executa. O dominio pede "envie evento", e Kafka executa. O dominio pede "notifique motorista", e WebSocket executa. O dominio pede "guarde status rapido", e Redis executa.

[PAGEBREAK]

## Capitulo 18 - Criacao de corrida no backend

O fluxo de criacao de corrida comeca quando o controller recebe um CreateRideRequest. Esse request vem do frontend com userId, startAddress e destinationAddress. O RideController transforma esse request em CreateRideCommand. O command e passado para CreateRideUseCase, cuja implementacao concreta e DefaultCreateRideUseCase.

Dentro de DefaultCreateRideUseCase, o primeiro passo e criar a entidade Ride usando Ride.newRide. Isso valida os dados e define o status inicial como CREATED. Depois, o caso de uso consulta accountClient.getById usando o userId. Essa consulta confirma se o cliente existe. Se o cliente nao existir, o caso de uso lanca IllegalArgumentException.

Depois de validar a conta, o caso de uso salva a corrida pelo RideGateway. A implementacao concreta converte Ride em RideEntity e salva no PostgreSQL. Em seguida, o caso de uso publica um evento usando sentEventService.sentEvent com RideCreatedMessage.from(aRide). Essa mensagem contem os dados necessarios para avisar que a corrida foi criada.

O caso de uso envolve a publicacao do evento em try e catch. Se Kafka falhar, o erro e registrado no log e relancado. Isso significa que a API nao esconde a falha de publicacao. Para um projeto mais avancado de producao, seria possivel usar outbox pattern para garantir consistencia entre banco e evento, mas para o desafio a estrategia atual deixa a falha visivel.

Na apresentacao, voce pode dizer que a criacao da corrida mostra o projeto inteiro comecando a se movimentar. Ela valida dados, consulta outro microsservico, salva no banco e inicia uma comunicacao assincrona via Kafka. Esse fluxo e um dos melhores para demonstrar a arquitetura.

[PAGEBREAK]

## Capitulo 19 - Aceite de corrida no backend

O aceite de corrida acontece quando o motorista clica em aceitar no frontend. A chamada chega em POST /rides/{id}/accept. O RideController recebe o id da URL e o driverId do corpo da requisicao. Ele cria AcceptRideCommand e chama AcceptRideUseCase. A implementacao concreta e DefaultAcceptRideUseCase.

O primeiro passo do caso de uso e consultar o Account Service pelo driverId. Se a conta nao existe, o sistema lanca erro. Se a conta existe, o caso de uso verifica se o tipo da conta e DRIVER. Isso impede que um cliente aceite uma corrida como se fosse motorista. Essa regra e importante porque o frontend sozinho nao deve ser a unica protecao.

Depois, o caso de uso busca a corrida pelo RideGateway. Se a corrida nao existe, lanca NoSuchElementException. Se existe, verifica se o status e CREATED. Caso nao seja, significa que a corrida ja foi aceita, cancelada ou finalizada. Nesse caso, o sistema lanca IllegalStateException. Essa regra evita aceitar uma corrida que nao esta mais disponivel.

Quando tudo esta valido, o caso de uso chama aRide.assignDriver. Esse metodo atribui o motorista, muda o status para IN_PROGRESS e atualiza updatedAt. Depois a corrida e salva no banco. Em seguida, o sistema tenta gravar o status no Redis. Se o Redis falhar, o erro e logado, mas a corrida continua salva no PostgreSQL. Depois o sistema tenta notificar via WebSocket. Se a notificacao falhar, o erro tambem e logado.

Essa decisao mostra que o PostgreSQL e a fonte principal de verdade. Redis e WebSocket sao importantes, mas complementares. Na apresentacao, explique que o aceite prioriza a consistencia do dado principal. A corrida aceita fica salva no banco, mesmo que uma notificacao temporaria falhe.

[PAGEBREAK]

## Capitulo 20 - Edicao, consulta de status e timeout de corridas

O caso de uso de edicao permite alterar origem e destino de uma corrida. Ele recebe UpdateRideCommand com rideId, userId, startAddress e destinationAddress. Primeiro valida se userId foi informado. Depois busca a corrida. Em seguida, verifica se o userId enviado e igual ao userId da corrida. Isso impede que um cliente edite corrida de outro cliente. Depois chama updateRoute na entidade Ride, salva a corrida e notifica motoristas sobre a atualizacao.

A consulta de status tem uma ideia importante: tentar cache primeiro. DefaultGetRideStatusUseCase chama rideStatusCache.get. Se encontrar dado no Redis, retorna status com source igual a redis. Se nao encontrar, busca a corrida no PostgreSQL e retorna source igual a database. Esse campo source e interessante porque mostra de onde veio a resposta. Na demonstracao, ele pode ajudar a explicar o papel do Redis.

O timeout e uma rotina automatica. RideTimeoutJob executa em intervalo configurado por ride.timeout.check-interval-ms. Ele calcula uma data limite subtraindo ride.timeout.seconds do horario atual. Depois chama TimeoutRidesUseCase com essa data. O caso de uso busca corridas com status CREATED criadas antes do limite. Para cada corrida expirada, muda o status para CANCELLED, salva no banco, atualiza Redis e notifica via WebSocket.

O repository usa o metodo findTop50ByStatusAndCreatedAtBefore. O Spring Data JPA interpreta esse nome e cria a consulta automaticamente. Buscar no maximo cinquenta por execucao evita que o job tente processar um volume ilimitado de uma vez. Para o desafio, isso e uma boa forma de mostrar preocupacao com controle de processamento.

Na apresentacao, diga que esses fluxos mostram regras alem do cadastro basico. O sistema controla propriedade da edicao, otimiza consulta de status com cache e executa cancelamento automatico de corridas antigas.

[PAGEBREAK]

## Capitulo 21 - Persistencia de corridas com PostgreSQL e JPA

O PostgreSQL e usado como banco persistente. Persistente significa que os dados sao armazenados de forma duravel. No projeto, contas ficam no banco account_db e corridas ficam no banco ride_db. O Docker Compose cria o container PostgreSQL e o arquivo SQL de inicializacao cria o banco ride_db.

No Ride Service, RideEntity e a classe JPA que representa a tabela tb_rides. Ela possui anotacoes Entity e Table. Cada campo da classe e mapeado para uma coluna. O id vira ride_id. O userId vira ride_user_id. O driverId vira ride_driver_id. A origem vira ride_start_address. O destino vira ride_destination_address. O status vira ride_status. As datas viram ride_created_at e ride_updated_at.

RideEntity tem dois metodos muito importantes. O metodo from recebe uma entidade de dominio Ride e cria uma entidade JPA. O metodo toAggregate recebe uma entidade JPA e reconstrui uma entidade de dominio Ride. Essa conversao separa banco e regra de negocio. A entidade de dominio nao tem anotacoes JPA. Ela nao precisa saber nome de tabela nem nome de coluna.

RideRepository estende JpaRepository. Isso significa que o Spring Data fornece automaticamente metodos como save, findById e findAll. Alem disso, o repository declara findTop50ByStatusAndCreatedAtBefore, que e usado no timeout. Esse metodo mostra como o Spring Data consegue criar consultas a partir do nome do metodo.

RidePostgresGateway implementa RideGateway. Ele e o adaptador que liga dominio e banco. Quando o caso de uso pede para salvar uma corrida, ele nao fala com JpaRepository diretamente. Ele fala com RideGateway. A implementacao concreta converte e salva. Esse desenho ajuda testes e manutencao.

[PAGEBREAK]

## Capitulo 22 - Kafka explicado no fluxo do projeto

Kafka e usado como broker de mensagens. Um broker de mensagens permite que uma parte do sistema publique um acontecimento e outra parte consuma esse acontecimento. No projeto, quando uma corrida e criada, o sistema publica uma mensagem no topico ride-topic. Depois, um consumidor recebe essa mensagem e envia notificacao para motoristas via WebSocket.

KafkaRideConfig cria o topico ride-topic. RideProducer e a classe que publica mensagens. Ela usa KafkaTemplate para enviar RideCreatedMessage. SentEventServiceImpl implementa o contrato SentEventService e chama RideProducer. Essa divisao permite que o caso de uso dependa de SentEventService, nao de KafkaTemplate diretamente.

RideConsumer e a classe que consome mensagens. O metodo consume tem a anotacao KafkaListener com topics igual a ride-topic. Quando uma mensagem chega, o Spring chama esse metodo automaticamente. O consumer transforma a mensagem em RideNotification e chama DriverNotifier. A implementacao atual de DriverNotifier envia via WebSocket.

No nivel iniciante, diga que Kafka funciona como uma fila de avisos. No nivel intermediario, explique que ele desacopla criacao de corrida e notificacao. No nivel avancado, diga que esse desenho facilita evolucao. No futuro, outros consumidores poderiam ouvir o mesmo topico para gerar metricas, auditoria ou historico, sem mudar o caso de uso de criacao.

Tambem e importante explicar por que nao chamar WebSocket diretamente no caso de uso de criacao. Seria possivel em um projeto pequeno, mas Kafka torna o fluxo mais flexivel. A criacao da corrida publica um evento, e os interessados reagem ao evento. Isso e uma pratica comum em arquiteturas orientadas a eventos.

[PAGEBREAK]

## Capitulo 23 - Redis explicado no fluxo do projeto

Redis e usado como cache de status. Cache e uma camada de armazenamento rapido, geralmente temporaria, usada para acelerar leituras. No projeto, o Redis guarda o status da corrida e o id do motorista em uma chave baseada no id da corrida. A chave usa o prefixo ride:status: seguido do id.

RideStatusCache e o contrato do dominio. RideStatusRedisCache e a implementacao com Redis. Quando uma corrida e aceita, DefaultAcceptRideUseCase chama rideStatusCache.put. Quando uma corrida expira por timeout, DefaultTimeoutRidesUseCase tambem atualiza o cache. O Redis guarda status e driverId em formato de hash. Depois aplica um TTL de vinte e quatro horas. TTL significa tempo de vida. Depois desse tempo, a chave pode expirar.

Quando alguem consulta o endpoint de status, DefaultGetRideStatusUseCase tenta ler Redis primeiro. Se encontrar, responde rapidamente e informa source redis. Se nao encontrar, busca no PostgreSQL e informa source database. Isso mostra uma estrategia conhecida como cache-aside. A aplicacao consulta cache, mas o banco continua sendo a fonte persistente.

Na apresentacao, deixe claro que Redis nao substitui PostgreSQL. Ele complementa. Se Redis cair ou perder uma chave, o status principal ainda esta salvo no banco. Isso e importante para evitar uma interpretacao errada de que o sistema depende somente do cache.

No nivel avancado, voce pode dizer que esse cache melhora tempo de resposta para consultas frequentes de status. Tambem mostra uma preocupacao com escalabilidade, mesmo que o projeto seja um desafio academico.

[PAGEBREAK]

## Capitulo 24 - WebSocket e STOMP explicados no projeto

WebSocket e a tecnologia usada para notificacoes em tempo real. Em HTTP comum, o navegador faz uma pergunta e o servidor responde. Depois a conexao acaba. Em WebSocket, existe uma conexao aberta, e o servidor consegue enviar mensagens para o navegador quando algo acontece. Isso e ideal para avisar motoristas sobre novas corridas.

WebSocketConfig habilita WebSocket com STOMP. O endpoint registrado e /ws. O broker simples usa o prefixo /topic. Isso significa que o backend pode publicar em topicos como /topic/rides, e clientes conectados podem assinar esse topico. STOMP e um protocolo de mensagens sobre WebSocket que organiza a comunicacao em destinos, como topicos.

WebSocketDriverNotifier implementa DriverNotifier. Ele usa SimpMessagingTemplate para enviar RideNotification ao topico /topic/rides. O dominio conhece apenas DriverNotifier. Ele nao sabe que por baixo existe STOMP, SimpMessagingTemplate ou WebSocket. Isso mostra novamente a separacao entre regra e infraestrutura.

No frontend, RideNotificationService usa RxStomp. Ele configura brokerURL com environment.wsUrl, define reconexao e heartbeat, ativa a conexao e assina /topic/rides. Quando recebe uma mensagem, transforma o corpo JSON em RideNotificationDto. A tela do motorista usa esse service para atualizar a lista de corridas disponiveis.

Na apresentacao, explique que WebSocket evita polling. Polling seria fazer o frontend perguntar a cada poucos segundos se existe corrida nova. Com WebSocket, o servidor avisa quando a corrida aparece. Isso reduz atraso e evita requisicoes repetidas desnecessarias.

[PAGEBREAK]

## Capitulo 25 - OpenFeign explicado no projeto

OpenFeign e usado para o Ride Service consultar o Account Service. O Ride Service precisa saber se o cliente existe quando cria uma corrida e se o motorista existe e e do tipo DRIVER quando aceita uma corrida. Como contas pertencem ao Account Service, o Ride Service faz uma chamada HTTP entre microsservicos.

AccountFeignClient declara essa chamada. Ele tem a anotacao FeignClient com name igual a account-service. O metodo getAccountById tem GetMapping para /accounts/{id}. Com isso, o codigo Java parece uma chamada de metodo comum, mas por baixo o Feign faz uma requisicao HTTP para o servico correto.

AccountClientImpl implementa o contrato AccountClient do dominio. Ele usa AccountFeignClient, recebe AccountFeignResponse e transforma em AccountData. Se o Account Service responder 404, o Feign lanca FeignException.NotFound. O codigo captura essa excecao e retorna Optional.empty. Isso permite que o caso de uso trate ausencia de conta de forma clara.

O ponto mais importante e que o dominio nao depende de Feign. Ele depende de AccountClient. Feign fica na infraestrutura. Isso facilita testes, porque nos testes unitarios dos casos de uso e possivel mockar AccountClient sem fazer chamada HTTP real.

Na apresentacao, diga que OpenFeign simplifica comunicacao entre microsservicos, mas foi encapsulado por uma interface do dominio para manter baixo acoplamento. Essa frase mostra entendimento de ferramenta e de arquitetura.

[PAGEBREAK]

## Capitulo 26 - Docker e Docker Compose explicados

Docker empacota aplicacoes em containers. Um container e um ambiente isolado com tudo que uma aplicacao precisa para rodar. Docker Compose permite subir varios containers juntos com um arquivo de configuracao. No projeto, o docker-compose.yml sobe PostgreSQL, Kafka, Redis, Config Server, Eureka Server, Account Service, Ride Service, API Gateway e frontend.

O container postgres usa a imagem postgres:16-alpine. Ele expoe a porta 5433 na maquina e usa 5432 internamente. Define usuario, senha e banco account_db. Tambem monta um volume chamado postgres_data para preservar dados e monta a pasta docker/init para executar scripts de inicializacao, como a criacao de ride_db.

O Kafka usa uma imagem da Confluent e roda em modo KRaft. O Redis usa redis:7-alpine. Os servicos Java sao construidos a partir dos Dockerfiles de cada modulo. O frontend e construido a partir do Dockerfile do Angular e servido por Nginx. O Gateway expoe a porta 8080, o Eureka expoe 8761 e o frontend expoe 4200.

O Compose usa healthchecks para verificar se os servicos estao saudaveis. Por exemplo, o PostgreSQL usa pg_isready. O Redis usa redis-cli ping. Os servicos Java usam endpoint de actuator health. O depends_on com condition service_healthy faz um servico esperar outro estar pronto antes de iniciar. Isso reduz problemas de subida em ordem errada.

Na apresentacao, explique que Docker Compose torna a demonstracao reproduzivel. Em vez de instalar tudo manualmente, o avaliador ou desenvolvedor consegue subir o ambiente com docker compose up --build. No nivel avancado, destaque que dentro da rede Docker os servicos se comunicam pelo nome do container, como postgres, kafka e redis, e nao por localhost.

[PAGEBREAK]

## Capitulo 27 - Angular explicado no projeto

Angular e o framework usado no frontend. Ele organiza a interface em componentes, rotas, services, formularios e estado reativo. O projeto usa Angular com componentes standalone, o que significa que cada componente declara os imports que precisa diretamente, sem depender de um modulo tradicional.

O arquivo main.ts e o ponto de entrada do frontend. Ele chama bootstrapApplication passando o componente App e a configuracao appConfig. Isso inicia a aplicacao Angular no navegador. O app.config.ts registra providers importantes. Ele define API_BASE_URL com environment.apiUrl, configura HttpClient com interceptor de erro, registra as rotas, configura PrimeNG e disponibiliza MessageService para mensagens toast.

O app.routes.ts define as rotas da aplicacao. A rota /login carrega a tela de login. A rota /client/rides carrega a tela do cliente, protegida por sessionGuard e role guard de CLIENT. A rota /driver/rides/available carrega a tela do motorista, protegida por sessionGuard e role guard de DRIVER. A rota /forbidden mostra acesso negado. A rota vazia usa um dashboard para redirecionar conforme o perfil.

O componente App monta a moldura geral da aplicacao. Ele mostra o titulo, a navegacao, a sidebar com a conta selecionada, o botao sair e o router-outlet. O router-outlet e o local onde a tela da rota atual aparece. O App tambem usa p-toast para mensagens globais.

Na apresentacao, diga que Angular foi escolhido porque facilita criar uma SPA organizada. SPA significa single page application, uma aplicacao que troca telas sem recarregar toda a pagina. O projeto usa recursos modernos como signals e computed para atualizar a interface de forma reativa.

[PAGEBREAK]

## Capitulo 28 - TypeScript, DTOs e services do frontend

TypeScript e a linguagem usada no Angular. Ele e uma evolucao do JavaScript com tipos. Tipos ajudam a saber quais campos existem em um objeto e reduzem erros. No projeto, api-dtos.ts define os formatos dos dados que circulam entre frontend e backend. AccountDto representa uma conta. RideDto representa uma corrida. CreateRideRequestDto representa o corpo usado para criar corrida. RideNotificationDto representa a mensagem recebida por WebSocket.

O arquivo api-routes.ts centraliza a montagem das URLs. Em vez de cada componente escrever strings de rota manualmente, o projeto usa funcoes para montar endpoints de contas, corridas, status e aceite. Isso reduz repeticao e diminui risco de erro de digitacao.

AccountService usa HttpClient para chamar endpoints de conta. Ele tem metodos para criar conta, listar contas e buscar conta por id. RideService faz o mesmo para corridas. Ele cria, edita, lista, busca por id, consulta status e aceita corrida. Esses services retornam Observables, que representam respostas assincronas. O componente se inscreve no Observable usando subscribe para reagir quando a resposta chega.

O token API_BASE_URL permite injetar a URL base da API. Isso evita espalhar environment.apiUrl por toda a aplicacao. Se a URL mudar, a configuracao fica concentrada.

Na apresentacao, explique que DTOs funcionam como moldes dos dados. Eles ajudam o frontend a saber que uma corrida tem id, userId, driverId, origem, destino, status e datas. Tambem ajudam a tratar casos como driverId nulo quando a corrida ainda nao tem motorista.

[PAGEBREAK]

## Capitulo 29 - Sessao, guards e erros no frontend

O SessionService controla a conta selecionada. Ele usa localStorage para manter a conta mesmo se a pagina for recarregada. Tambem usa signal para manter o estado reativo dentro da aplicacao. O metodo login salva a conta no localStorage e atualiza o signal. O metodo logout remove a conta e limpa o signal. O computed type retorna o tipo da conta atual, que pode ser CLIENT, DRIVER ou null.

E importante dizer que isso nao e autenticacao real. Nao existe senha, token JWT nem validacao de permissao no backend. Para o desafio, a sessao serve para escolher um perfil e demonstrar o fluxo. Em uma evolucao real, seria necessario implementar login seguro, tokens e autorizacao no backend.

O sessionGuard impede acessar rotas internas sem conta selecionada. Se nao houver conta, ele redireciona para /login. O role guard verifica o tipo da conta. Se uma rota exige CLIENT e o usuario e DRIVER, ele redireciona para /forbidden. Isso melhora a experiencia e evita que um usuario navegue para tela errada pelo frontend.

O errorInterceptor captura erros HTTP. Quando uma chamada falha, ele usa getMessageForHttpError para transformar status e corpo de erro em mensagem amigavel. Depois usa MessageService para mostrar um toast. O arquivo error-messages.ts define mensagens para 400, 404, 409 e 500. Se o backend envia mensagens no corpo errors, o frontend tenta mostrar essas mensagens.

Na apresentacao, explique que o frontend tem tratamento centralizado de erro. Isso evita repetir o mesmo tratamento em cada componente. Tambem deixa a experiencia mais clara para o usuario.

[PAGEBREAK]

## Capitulo 30 - Tela de login explicada

A tela de login fica em login.component.ts, login.component.html e login.component.css. Ela permite escolher uma conta existente ou criar uma nova conta. O usuario pode alternar entre cliente e motorista. A lista exibida muda conforme o tipo selecionado.

No TypeScript, o componente injeta AccountService, SessionService, Router, DestroyRef e FormBuilder. AccountService busca e cria contas. SessionService salva a conta selecionada. Router navega depois da entrada. DestroyRef ajuda a encerrar assinaturas quando o componente e destruido. FormBuilder cria o formulario reativo de cadastro.

O componente usa signals para controlar estado. accounts guarda a lista de contas. selectedType guarda CLIENT ou DRIVER. loading indica carregamento. createDialogVisible controla se o modal esta aberto. creating indica se o cadastro esta sendo enviado. O computed filteredAccounts filtra a lista de contas pelo tipo selecionado.

O formulario de criacao tem name, email e type. O nome e obrigatorio. O email e obrigatorio e precisa ter formato valido. O type e obrigatorio. Quando o usuario envia o formulario, o componente valida os campos, chama createAccount, fecha o dialog e entra automaticamente com a conta criada.

No HTML, a tela mostra uma area visual do projeto, os botoes de perfil, a lista de contas, o botao de atualizar, o botao de criar conta e o dialog de cadastro. Na apresentacao, explique que essa tela substitui um login real para facilitar a demonstracao dos perfis. Ela e uma selecao de contexto para o desafio.

[PAGEBREAK]

## Capitulo 31 - Tela do cliente explicada

A tela do cliente fica em client-rides.component.ts, client-rides.component.html e client-rides.component.css. Ela permite ao cliente ver suas corridas, criar uma nova corrida e editar origem e destino quando a corrida ainda pode ser editada.

O componente injeta RideService, SessionService, MessageService, DestroyRef e FormBuilder. RideService chama a API de corridas. SessionService informa a conta atual. MessageService mostra toasts de sucesso ou erro. FormBuilder cria formularios de criacao e edicao.

O estado principal e rides, que guarda a lista de corridas carregadas. O computed myRides filtra apenas as corridas cujo userId e igual ao id da conta atual. Isso faz a tela mostrar as corridas do cliente selecionado. loading controla carregamento. createDialogVisible controla o dialog de nova corrida. creating controla envio. editingRideId informa qual corrida esta em edicao. savingRideId informa qual corrida esta sendo salva.

Quando o cliente cria uma corrida, o componente pega origem e destino do formulario, pega o id da conta atual e chama rideService.createRide. Depois que a API responde, ele fecha o dialog, mostra mensagem de sucesso e recarrega a lista. No backend, esse simples clique inicia um fluxo grande: valida cliente, salva corrida, publica Kafka e notifica motorista.

Quando o cliente edita uma corrida, o componente verifica se a corrida pode ser editada. O metodo canEditRide retorna falso se o status for COMPLETED ou CANCELLED. Ao salvar, o componente chama updateRide e atualiza a lista local com a corrida retornada pela API. Na apresentacao, destaque que o frontend tambem protege a interface, mas a regra principal de permissao e status fica no backend.

[PAGEBREAK]

## Capitulo 32 - Tela do motorista explicada

A tela do motorista fica em driver-available-rides.component.ts, driver-available-rides.component.html e driver-available-rides.component.css. Ela mostra corridas disponiveis e permite aceitar uma corrida.

O componente injeta RideService, RideNotificationService, SessionService, MessageService e DestroyRef. RideService lista e aceita corridas. RideNotificationService conecta no WebSocket e recebe mensagens em tempo real. SessionService informa o motorista atual. MessageService mostra mensagens. DestroyRef encerra assinaturas quando o componente sai da tela.

O estado rides guarda as corridas conhecidas pela tela. O computed availableRides filtra apenas corridas com status CREATED e sem driverId. Isso significa que a tela mostra apenas corridas realmente disponiveis para aceite. acceptingRideId indica qual corrida esta sendo aceita, para bloquear cliques repetidos e mostrar loading no botao correto.

No constructor, o componente chama loadRides e watchRideNotifications. O loadRides busca o estado atual pela API. O watchRideNotifications assina o topico de WebSocket. Isso e importante porque a tela combina duas estrategias. Primeiro carrega o que ja existe. Depois continua recebendo novidades em tempo real.

Quando chega uma notificacao, handleNotification decide o que fazer. Se a notificacao nao esta em CREATED ou ja tem motorista, a corrida e removida da lista. Isso acontece, por exemplo, quando alguem aceita a corrida. Se a notificacao representa uma corrida disponivel, ela e convertida em RideDto e adicionada no topo da lista. O usuario tambem recebe um toast dizendo que ha nova corrida disponivel.

Na apresentacao, essa tela e ideal para demonstrar WebSocket. Abra a tela do motorista, depois crie uma corrida como cliente. A corrida deve aparecer para o motorista sem precisar atualizar manualmente.

[PAGEBREAK]

## Capitulo 33 - Componentes compartilhados e helpers

O projeto tem componentes e helpers compartilhados para evitar repeticao. PageHeaderComponent e um componente de cabecalho usado nas telas principais. Ele recebe titulo e subtitulo e permite inserir acoes, como botoes de atualizar ou nova corrida. Isso evita copiar a mesma estrutura de cabecalho em varias telas.

RideStatusTagComponent mostra o status da corrida como uma tag visual do PrimeNG. Ele recebe o status e a perspectiva, que pode ser client ou driver. A perspectiva importa porque a mesma informacao pode ser exibida com palavras diferentes. Para o cliente, CREATED significa "Aguardando motorista". Para o motorista, CREATED significa "Disponivel". O componente usa computed para recalcular label e severidade quando as entradas mudam.

O arquivo ride-view.helpers.ts centraliza funcoes visuais. Ele define labels de status para cliente e motorista, severidades de tag, funcao para encurtar id, funcao para mostrar motorista e funcao de trackBy para tabelas. Essa centralizacao evita que cada tela implemente sua propria versao da mesma regra visual.

Os arquivos de estilo, como styles.css, colors.css e CSS de cada componente, definem a aparencia da aplicacao. O CSS global cuida da base visual. O CSS de cada componente cuida dos detalhes da tela correspondente. Essa organizacao ajuda a manter o visual consistente sem misturar tudo em um arquivo unico.

Na apresentacao, explique que componentes compartilhados e helpers reduzem duplicacao. Isso torna o codigo mais facil de manter. Se a label de um status mudar, por exemplo, a alteracao pode ser feita no helper central, e nao em varias telas.

[PAGEBREAK]

## Capitulo 34 - PrimeNG, RxJS e experiencia do usuario

PrimeNG e a biblioteca de componentes visuais usada no frontend. Ela fornece botoes, dialogos, tabelas, tags, inputs, selects e toasts. Usar uma biblioteca assim acelera o desenvolvimento e deixa a interface mais consistente. No projeto, PrimeNG aparece em imports como Button, Dialog, InputText, Select, TableModule, Tag e Toast.

RxJS e usado porque chamadas HTTP e mensagens WebSocket sao assincronas. Quando o frontend chama uma API, a resposta nao chega imediatamente. O metodo retorna um Observable. O componente usa subscribe para executar uma acao quando a resposta chega. O operador finalize e usado para desligar loading depois que a chamada termina, com sucesso ou erro. O takeUntilDestroyed evita que assinaturas continuem vivas depois que o componente for destruido.

A experiencia do usuario foi pensada com estados de carregamento, mensagens de sucesso, mensagens de erro e bloqueio de botoes durante operacoes. Quando uma corrida esta sendo aceita, o botao mostra loading. Quando uma corrida e criada, aparece toast de sucesso. Quando a API retorna erro, o interceptor mostra uma mensagem amigavel.

Na apresentacao, explique que frontend nao e apenas desenhar tela. Ele tambem precisa controlar estado, chamadas assincronas, validacao, permissao visual e resposta ao usuario. Angular, RxJS e PrimeNG trabalham juntos para entregar essa experiencia.

No nivel avancado, destaque que a tela do motorista e reativa em dois sentidos. Ela reage a chamadas HTTP iniciais e tambem reage a eventos WebSocket que chegam depois. Isso torna a interface mais proxima de uma aplicacao em tempo real.

[PAGEBREAK]

## Capitulo 35 - Testes do backend explicados

Os testes do backend usam JUnit, Mockito, Testcontainers e JaCoCo. JUnit organiza e executa os testes. Mockito cria objetos falsos, chamados mocks, para isolar uma unidade de codigo. Testcontainers sobe um PostgreSQL real em Docker para testes de integracao. JaCoCo mede cobertura de testes.

Os testes de entidade verificam regras internas. AccountTest valida se a entidade Account impede dados invalidos. RideTest valida criacao de corrida, atribuicao de motorista, edicao de rota e regras de status. Esses testes sao importantes porque as entidades concentram regras fundamentais.

Os testes de casos de uso verificam a aplicacao sem depender de banco real. DefaultCreateAccountUseCaseTest, DefaultGetAccountByIdUseCaseTest e DefaultListAccountsUseCaseTest testam o Account Service. No Ride Service, existem testes para criar corrida, aceitar corrida, atualizar corrida, buscar por id, listar, consultar status e executar timeout. Como usam mocks, esses testes sao rapidos e focados na regra.

Os testes de integracao, como AccountPostgresGatewayIT e RidePostgresGatewayIT, validam a persistencia com PostgreSQL real. Isso e melhor do que usar um banco em memoria quando o objetivo e verificar comportamento real de banco, mapeamento JPA e queries. O Testcontainers cria um banco descartavel para o teste e remove depois.

O JaCoCo exige cobertura minima em dominio e aplicacao. A infraestrutura fica fora do gate principal porque controllers, adapters e configuracoes podem ser testados por integracao e E2E. Na apresentacao, diga que a estrategia prioriza regras de negocio com testes unitarios e valida pontos de integracao com banco real.

[PAGEBREAK]

## Capitulo 36 - Testes do frontend, Postman e CI

No frontend, os testes unitarios usam Jasmine e Karma. Jasmine define os testes e expectativas. Karma executa os testes no navegador. Os testes verificam componentes, helpers e interceptadores. Por exemplo, o teste do RideStatusTagComponent valida se o status aparece com o texto correto. O teste do ClientRidesComponent valida filtro, criacao, edicao e regras de formulario. O teste do errorInterceptor valida se mensagens de erro sao mostradas corretamente.

Cypress e usado para teste E2E de smoke. Smoke test e um teste rapido que verifica se o fluxo basico funciona. O arquivo smoke.cy.ts verifica se visitante sem sessao e redirecionado para login, se a tela de login aparece, se o filtro por perfil funciona e se entrar como cliente leva para a tela de corridas. O teste usa cy.intercept para simular respostas da API, entao pode rodar sem backend de pe.

Postman e usado para demonstrar a API manualmente. A collection em postman permite criar cliente, criar motorista, criar corrida, aceitar corrida e consultar status. Isso e util para mostrar que o backend funciona independentemente do frontend.

O GitHub Actions executa CI. CI significa integracao continua. A cada push ou pull request na branch main, o pipeline roda testes e build. O job do backend configura JDK 17 e executa Maven verify. O job do frontend configura Node 22, instala dependencias com npm ci, roda testes headless e gera build de producao.

Na apresentacao, explique que qualidade nao depende apenas de clicar manualmente na tela. O projeto tem testes automatizados em diferentes niveis e pipeline para validar em ambiente limpo.

[PAGEBREAK]

## Capitulo 37 - Fluxo completo de criacao de corrida para apresentar

Para apresentar a criacao de corrida, comece pela tela do cliente. O cliente preenche origem e destino no formulario de nova corrida. O componente ClientRidesComponent valida os campos e chama RideService.createRide. Esse service monta uma chamada HTTP para o endpoint /rides usando a URL base do Gateway.

A requisicao chega ao API Gateway em localhost:8080. Como a rota comeca com /rides, o Gateway encaminha para o Ride Service. O RideController recebe CreateRideRequest, monta CreateRideCommand e chama DefaultCreateRideUseCase. O caso de uso cria a entidade Ride com status CREATED e driverId nulo.

Depois, o caso de uso consulta o Account Service usando AccountClient, cuja implementacao usa Feign. Essa etapa confirma que o cliente existe. Se o cliente nao existir, a criacao falha. Se existir, a corrida e salva no PostgreSQL por meio de RideGateway, RidePostgresGateway, RideEntity e RideRepository.

Com a corrida salva, o caso de uso publica RideCreatedMessage usando SentEventService. A implementacao envia para Kafka pelo RideProducer. O RideConsumer escuta o topico ride-topic, recebe a mensagem e chama DriverNotifier. A implementacao WebSocketDriverNotifier envia a notificacao para /topic/rides.

No frontend do motorista, RideNotificationService esta inscrito nesse topico. Quando a mensagem chega, DriverAvailableRidesComponent transforma a notificacao em RideDto e adiciona a corrida na lista de disponiveis. O motorista ve a nova corrida em tempo real.

Essa explicacao e uma das mais importantes do projeto, porque passa por quase todas as tecnologias. Ela mostra Angular, Gateway, Ride Service, Feign, Account Service, PostgreSQL, Kafka, WebSocket e tela do motorista.

[PAGEBREAK]

## Capitulo 38 - Fluxo completo de aceite de corrida para apresentar

Para apresentar o aceite de corrida, comece pela tela do motorista. O motorista visualiza uma corrida disponivel e clica em aceitar. O componente DriverAvailableRidesComponent pega o id da corrida e o id da conta atual. Depois chama RideService.acceptRide, enviando uma requisicao POST para /rides/{id}/accept.

A requisicao passa pelo Gateway e chega ao RideController. O controller cria AcceptRideCommand com rideId e driverId. Depois chama DefaultAcceptRideUseCase. O caso de uso consulta o Account Service para validar se o motorista existe. Em seguida, verifica se o tipo da conta e DRIVER. Essa validacao e fundamental, porque impede um cliente de aceitar corrida.

Depois o caso de uso busca a corrida. Se ela nao existir, responde como recurso nao encontrado. Se existir, verifica se o status ainda e CREATED. Se a corrida ja foi aceita, cancelada ou finalizada, o sistema impede novo aceite. Se estiver disponivel, chama assignDriver na entidade Ride. Esse metodo atribui o motorista, muda status para IN_PROGRESS e atualiza a data.

A corrida atualizada e salva no PostgreSQL. Depois o sistema grava o status no Redis para consultas rapidas. Em seguida, envia notificacao via WebSocket. A tela do motorista remove a corrida da lista de disponiveis, porque ela ja nao esta mais aberta para aceite.

Na apresentacao, destaque que o aceite tem regra de negocio importante. Nao basta atualizar uma tabela. O sistema precisa validar perfil, validar status, salvar o estado, atualizar cache e avisar as telas.

[PAGEBREAK]

## Capitulo 39 - Fluxo completo de timeout para apresentar

O timeout existe para impedir que corridas fiquem abertas para sempre. A configuracao do Ride Service define quantos segundos uma corrida pode ficar aguardando aceite e de quanto em quanto tempo o job deve verificar. No projeto, a configuracao padrao usa cento e vinte segundos para timeout e trinta segundos para intervalo de verificacao.

RideTimeoutJob e um componente agendado. A anotacao Scheduled faz o metodo execute rodar automaticamente. Ele calcula uma data limite subtraindo o timeout do horario atual. Depois cria TimeoutRidesCommand e chama TimeoutRidesUseCase.

DefaultTimeoutRidesUseCase busca corridas expiradas usando RideGateway.findCreatedBefore. A implementacao em RidePostgresGateway chama RideRepository.findTop50ByStatusAndCreatedAtBefore, procurando corridas com status CREATED e data de criacao anterior ao limite. Para cada corrida encontrada, o caso de uso muda o status para CANCELLED, salva no banco, atualiza Redis e notifica via WebSocket.

Esse fluxo mostra que o sistema nao depende apenas de acoes do usuario. Ele tambem executa processos internos automaticamente. Isso e comum em sistemas reais, onde tarefas agendadas limpam dados, expiram pedidos, enviam notificacoes ou geram relatorios.

Na apresentacao, diga que o timeout protege a consistencia da experiencia. Uma corrida que nunca foi aceita nao deve continuar aparecendo como disponivel indefinidamente. O job automatiza esse controle e mantem banco, cache e frontend sincronizados.

[PAGEBREAK]

## Capitulo 40 - Como explicar arquitetura limpa sem usar termos dificeis

Se a banca for iniciante, explique a arquitetura dizendo que o projeto separa responsabilidades. Uma parte representa as regras do negocio. Outra parte executa acoes do sistema. Outra parte conversa com tecnologias externas. Essa explicacao e suficiente para mostrar organizacao sem usar muitos termos tecnicos.

Se a banca tiver conhecimento intermediario, diga que o projeto esta dividido em domain, application e infrastructure. O domain contem entidades, enums e interfaces. O application contem casos de uso. A infrastructure contem controllers, banco, Kafka, Redis, WebSocket, Feign e configuracoes. Essa divisao evita misturar regra com detalhes de tecnologia.

Se a banca perguntar de forma avancada, explique inversao de dependencia. A regra de negocio depende de contratos, nao de implementacoes concretas. DefaultCreateRideUseCase depende de RideGateway, AccountClient e SentEventService. Ele nao depende de JpaRepository, FeignClient ou KafkaTemplate. Isso torna a regra mais testavel e flexivel.

Use exemplos simples. RideGateway e o contrato que diz que o sistema precisa salvar e buscar corridas. RidePostgresGateway e a implementacao que faz isso usando PostgreSQL. DriverNotifier e o contrato que diz que o sistema precisa notificar motoristas. WebSocketDriverNotifier e a implementacao que faz isso usando WebSocket.

Essa forma de explicar mostra maturidade. Voce nao precisa falar apenas nomes de tecnologias. Voce mostra por que o codigo foi organizado desse jeito e como isso ajuda testes, manutencao e evolucao.

[PAGEBREAK]

## Capitulo 41 - Perguntas provaveis e respostas em forma de explicacao

Se perguntarem por que usar Kafka se dava para chamar WebSocket diretamente, responda que em um projeto pequeno seria possivel chamar WebSocket direto, mas Kafka desacopla a criacao da corrida da notificacao. O caso de uso publica um evento, e qualquer interessado pode reagir a esse evento. Hoje o consumidor envia notificacao. No futuro, outro consumidor poderia gerar auditoria, metricas ou historico sem alterar a criacao da corrida.

Se perguntarem por que usar Redis se o dado ja esta no PostgreSQL, explique que Redis e cache, nao fonte principal. Ele guarda uma copia temporaria do status para leitura rapida. Se o cache nao tiver o dado, o sistema consulta o PostgreSQL. Assim, o banco continua sendo a fonte persistente e o Redis apenas melhora consulta.

Se perguntarem se a sessao do frontend e autenticacao real, responda que nao. A sessao com localStorage simula selecao de perfil para o desafio. Ela permite demonstrar cliente e motorista sem implementar login completo. Em uma evolucao real, seria necessario usar autenticacao com senha, token JWT, refresh token e autorizacao no backend.

Se perguntarem sobre dois motoristas aceitando ao mesmo tempo, explique que o caso de uso valida se a corrida ainda esta CREATED antes de aceitar. Para um ambiente de alta concorrencia real, voce reforcaria essa regra com lock otimista, versionamento ou update condicional no banco. Isso mostra que voce entende a solucao atual e tambem sabe como evoluir.

Se perguntarem o que voce melhoraria, responda que adicionaria autenticacao real, autorizacao no backend, migracoes com Flyway, paginacao, lock de concorrencia no aceite, observabilidade com logs estruturados e tracing, alem de outbox pattern para eventos mais confiaveis.

[PAGEBREAK]

## Capitulo 42 - Roteiro de fala para apresentacao

Voce pode abrir dizendo que o Ride Challenge e uma aplicacao full stack de corridas, com frontend em Angular e backend em microsservicos Java usando Spring Boot e Spring Cloud. O objetivo e simular um fluxo realista em que clientes criam corridas, motoristas recebem essas corridas em tempo real e podem aceitar uma delas.

Depois explique a arquitetura geral. Diga que o frontend chama o API Gateway. O Gateway roteia para Account Service ou Ride Service. O Config Server centraliza configuracoes. O Eureka permite que os servicos se encontrem pelo nome. O PostgreSQL guarda dados persistentes. O Kafka transporta eventos. O Redis guarda cache de status. O WebSocket atualiza a tela do motorista em tempo real.

Em seguida, fale do Account Service. Explique que ele gerencia contas de clientes e motoristas. A entidade Account valida dados. Os casos de uso criam, listam e buscam contas. A infraestrutura expoe endpoints REST e salva no PostgreSQL.

Depois fale do Ride Service. Explique que ele e o nucleo do sistema. Ele cria corrida, valida cliente consultando Account Service, salva no banco, publica evento no Kafka, consome evento, notifica motoristas, aceita corrida, atualiza Redis e cancela corridas antigas por timeout.

Depois mostre o frontend. Explique que Angular organiza rotas e componentes. A tela de login seleciona perfil. A tela do cliente cria e edita corridas. A tela do motorista lista corridas disponiveis e recebe notificacoes WebSocket. Os services Angular chamam a API e os DTOs tipam os dados.

Feche falando de qualidade. Diga que o projeto tem testes unitarios no backend com JUnit e Mockito, testes de integracao com Testcontainers, testes no frontend com Jasmine e Karma, smoke E2E com Cypress, collection Postman e pipeline de CI com GitHub Actions.

[PAGEBREAK]

## Capitulo 43 - Explicacao final para memorizar

Se voce precisar resumir o projeto inteiro em uma fala curta, diga que o sistema foi criado para demonstrar um fluxo de corridas em tempo real. O cliente cria uma corrida no Angular. A chamada passa pelo API Gateway e chega ao Ride Service. O Ride Service valida a conta no Account Service, salva a corrida no PostgreSQL e publica um evento no Kafka. O consumidor Kafka recebe esse evento e usa WebSocket para avisar os motoristas conectados. Quando um motorista aceita, o backend valida se ele e motorista, muda o status da corrida, salva no banco, atualiza Redis e notifica novamente.

Se quiser resumir as tecnologias, diga que Angular entrega a interface, Java e Spring Boot entregam os microsservicos, Spring Cloud Config centraliza configuracao, Eureka faz descoberta de servicos, Gateway centraliza entrada, PostgreSQL persiste dados, Kafka trabalha com eventos, Redis atua como cache, WebSocket entrega tempo real, Docker Compose sobe o ambiente e os testes garantem qualidade.

Se quiser resumir a arquitetura, diga que o codigo foi dividido em camadas. O dominio contem as regras e contratos. A aplicacao contem os casos de uso. A infraestrutura contem detalhes externos. Essa separacao torna o projeto mais organizado, mais testavel e mais facil de evoluir.

Se quiser fechar de forma forte, diga que o principal valor do projeto e integrar varias tecnologias com responsabilidades claras. Ele nao e apenas um CRUD. Ele mostra fluxo em tempo real, microsservicos, evento assincrono, cache, persistencia, Docker e testes automatizados trabalhando juntos.

Essa e a ideia que voce deve levar para a apresentacao. Nao tente decorar cada linha. Entenda a funcao de cada parte e explique como elas se conectam. Quando voce entende o caminho da corrida, desde o clique no frontend ate a notificacao no motorista, voce consegue responder a maioria das perguntas.
