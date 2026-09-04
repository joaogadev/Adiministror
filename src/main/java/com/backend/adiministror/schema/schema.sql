CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS gasto_extra CASCADE;
DROP TABLE IF EXISTS pagamento CASCADE;
DROP TABLE IF EXISTS contrato CASCADE;
DROP TABLE IF EXISTS alugueis CASCADE;
DROP TABLE IF EXISTS tenant CASCADE;
DROP TABLE IF EXISTS salas CASCADE;
DROP TABLE IF EXISTS galeria CASCADE;
DROP TABLE IF EXISTS endereco CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;


-- =====================================================
-- USUARIO
-- =====================================================

CREATE TABLE usuario (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         nome VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         senha VARCHAR(255) NOT NULL,
                         role VARCHAR(30) NOT NULL DEFAULT 'DONO',
                         phone VARCHAR(20) NOT NULL,

                         CONSTRAINT user_role_check
                             CHECK (
                                 role IN (
                                          'ADMIN',
                                          'DONO'
                                     )
                                 )
);


-- =====================================================
-- ENDERECO
-- =====================================================

CREATE TABLE endereco (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          zip_code VARCHAR(8) NOT NULL,
                          estado VARCHAR(2) NOT NULL,
                          cidade VARCHAR(255) NOT NULL,
                          bairro VARCHAR(255) NOT NULL,
                          rua VARCHAR(255) NOT NULL,
                          numero VARCHAR(10) NOT NULL,
                          complemento VARCHAR(255),
                          latitude NUMERIC(10, 8),
                          longitude NUMERIC(11, 8),
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- =====================================================
-- GALERIA
-- =====================================================

CREATE TABLE galeria (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         nome VARCHAR(255) NOT NULL,
                         phone VARCHAR(20),
                         endereco_id UUID NOT NULL UNIQUE
                             REFERENCES endereco(id),

                         dono_id UUID NOT NULL
                             REFERENCES usuario(id)
);


-- =====================================================
-- SALAS
-- =====================================================

CREATE TABLE salas (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       nome VARCHAR(255) NOT NULL,
                       galeria_id UUID NOT NULL
                           REFERENCES galeria(id)
);


-- =====================================================
-- TENANT
-- Inquilino atual ou histórico.
-- =====================================================

CREATE TABLE tenant (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        name VARCHAR(150) NOT NULL,
                        phone VARCHAR(20),
                        email VARCHAR(150),
                        document_number VARCHAR(14) UNIQUE,
                        document_type VARCHAR(11),
                        ativo BOOLEAN NOT NULL DEFAULT TRUE,
                        CONSTRAINT tenant_document_type_check
                            CHECK (
                                document_type IN (
                                                  'CPF',
                                                  'CNPJ'
                                    )
                                )
);


-- =====================================================
-- ALUGUEIS
--
-- Representa o vínculo entre uma Sala e um Tenant.
-- =====================================================

CREATE TABLE alugueis (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          sala_id UUID NOT NULL
                              REFERENCES salas(id),

                          inquilino_id UUID NOT NULL
                              REFERENCES tenant(id),

                          data_inicio DATE NOT NULL DEFAULT CURRENT_DATE,
                          dia_vencimento_padrao SMALLINT NOT NULL,
                          valor_mensal NUMERIC(12, 2) NOT NULL,
                          status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
                          CONSTRAINT aluguel_dia_vencimento_check
                              CHECK (
                                  dia_vencimento_padrao BETWEEN 1 AND 31
                                  ),

                          CONSTRAINT aluguel_valor_check
                              CHECK (
                                  valor_mensal >= 0
                                  ),

                          CONSTRAINT aluguel_status_check
                              CHECK (
                                  status IN (
                                             'ATIVO',
                                             'ENCERRADO'
                                      )
                                  )
);


-- =====================================================
-- GARANTIR APENAS UM ALUGUEL ATIVO POR SALA
--
-- Permite:
-- Sala 01 -> João   (ENCERRADO)
-- Sala 01 -> Carlos (ENCERRADO)
-- Sala 01 -> Pedro  (ATIVO)
--
-- Mas não permite dois ATIVOS simultaneamente.
-- =====================================================

CREATE UNIQUE INDEX uq_aluguel_ativo_sala
    ON alugueis(sala_id)
    WHERE status = 'ATIVO';


-- Um Tenant também não pode possuir dois aluguéis ativos.
-- Caso futuramente queira permitir isso, basta remover
-- este índice.

CREATE UNIQUE INDEX uq_aluguel_ativo_tenant
    ON alugueis(inquilino_id)
    WHERE status = 'ATIVO';


-- =====================================================
-- CONTRATO
-- Um aluguel pode possuir vários contratos ao longo
-- do tempo devido às renovações.
-- =====================================================

CREATE TABLE contrato (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          aluguel_id UUID NOT NULL
                              REFERENCES alugueis(id),

                          data_inicio DATE NOT NULL,
                          data_fim DATE NOT NULL,
                          aviso_antecedencia_dias INTEGER NOT NULL DEFAULT 30,
                          status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT contrato_datas_check
                              CHECK (
                                  data_fim > data_inicio
                                  ),

                          CONSTRAINT contrato_aviso_check
                              CHECK (
                                  aviso_antecedencia_dias >= 0
                                  ),

                          CONSTRAINT contrato_status_check
                              CHECK (
                                  status IN (
                                             'ATIVO',
                                             'RENOVADO',
                                             'ENCERRADO'
                                      )
                                  )
);


-- =====================================================
-- PAGAMENTO
-- Cada registro representa UMA mensalidade.
-- Exemplo:
-- competencia     = 2026-09-01
-- data_vencimento = 2026-09-10
-- =====================================================

CREATE TABLE pagamento (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           aluguel_id UUID NOT NULL
                               REFERENCES alugueis(id),

                           competencia DATE NOT NULL,
                           valor NUMERIC(12, 2) NOT NULL,
                           data_vencimento DATE NOT NULL,
                           data_pagamento DATE,
                           status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
                           aviso_7_dias_enviado BOOLEAN NOT NULL DEFAULT FALSE,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT pagamento_valor_check
                               CHECK (
                                   valor >= 0
                                   ),

                           CONSTRAINT pagamento_status_check
                               CHECK (
                                   status IN (
                                              'PENDENTE',
                                              'PAGO',
                                              'ATRASADO'
                                       )
                                   ),

                           CONSTRAINT pagamento_competencia_unique
                               UNIQUE (
                                       aluguel_id,
                                       competencia
                                   )
);


-- =====================================================
-- GASTOS EXTRAS
-- Gasto sempre pertence a uma galeria.
-- Pode opcionalmente estar relacionado a uma sala.
-- =====================================================

CREATE TABLE gasto_extra (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             galeria_id UUID NOT NULL
                                 REFERENCES galeria(id),

                             sala_id UUID
                                 REFERENCES salas(id),

                             nome VARCHAR(150) NOT NULL,
                             descricao TEXT,
                             valor NUMERIC(12, 2) NOT NULL,
                             data_gasto DATE NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT gasto_extra_valor_check
                                 CHECK (
                                     valor >= 0
                                     )
);