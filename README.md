# NetWatch

Aplicação desktop JavaFX para inventário e demonstração de monitoramento de switches. A primeira versão funciona em modo de demonstração, não se conecta à rede e grava os dados em SQLite no perfil local do usuário (`~/.netwatch/netwatch.db`). Os endereços `192.0.2.x` são reservados para documentação.

## Requisitos

- JDK 21
- Maven 3.9+
- Windows, Linux ou macOS com suporte JavaFX

## Executar

```bash
mvn clean test
mvn clean package
mvn javafx:run
```

No Windows, execute os comandos no PowerShell, dentro desta pasta. O Maven baixa as dependências na primeira execução; a aplicação em si não requer internet depois disso.

## O que funciona nesta versão

- Dashboard com totais e gráfico por estado, alimentado pelo SQLite.
- Inventário persistente com cadastro de switches.
- Simulação manual de transição UP/DOWN com histórico sem eventos duplicados quando o estado não muda.
- Limpeza local de eventos com confirmação e registro em tabela de auditoria separada.
- Relatório TXT UTF-8 com CRLF, seleção de diretório, nome com data/hora e prevenção de sobrescrita.
- Três equipamentos demonstrativos criados no primeiro início.

## Arquitetura e limites

`NetWatchApp` contém a interface JavaFX e `Store` encapsula o acesso JDBC e as regras demonstrativas. As dependências SNMP4J e SSHJ estão declaradas para permitir evolução, mas nenhum adaptador real está implementado ou ativado. Não há polling automático, telas dedicadas para portas/dispositivos/logs, importação CSV ou cálculo confiável de disponibilidade. O relatório registra essa limitação e não presume disponibilidade em lacunas sem coleta.

Os dados de demonstração são fictícios. Para operação real, implemente provedores separados, cadastre alvos explicitamente, valide a identidade SSH, proteja segredos e use somente OIDs documentados. Não use sem autorização do responsável pela rede.

## Banco e segurança

O SQLite é criado no diretório `.netwatch` do perfil do usuário. O esquema inicial contém switches, eventos e auditoria local. Não armazene credenciais nesta versão. A limpeza CLEAR LOG exclui apenas eventos da aplicação e preserva a auditoria.

## Licença

MIT. Consulte [LICENSE](LICENSE).
