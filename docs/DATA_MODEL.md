# Modelo de dados inicial

- `switches`: identidade, nome, modelo, endereço, localização, estado e horário da última atualização.
- `events`: transições de estado com horário e descrição; ligado ao switch por chave estrangeira.
- `audit`: operações administrativas, independente dos eventos apagáveis.

Horários são gravados em UTC ISO-8601. O relatório apresenta horário local de geração. Migrações versionadas e entidades de interfaces/dispositivos são pendências.
