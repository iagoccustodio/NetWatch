# Arquitetura

A versão inicial separa a janela JavaFX (`NetWatchApp`) do acesso a dados (`Store`). SQLite persiste o inventário, transições e auditoria. `Store.transition` só registra evento quando o estado muda. A camada de UI usa o serviço local sem rede. As dependências SNMP4J e SSHJ estão presentes para evolução, sem implementação real nesta entrega.
