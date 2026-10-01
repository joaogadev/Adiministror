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

--
-- PostgreSQL database dump
--

-- Dumped from database version 18.3
-- Dumped by pg_dump version 18.4

-- Started on 2026-09-26 22:04:02

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2 (class 3079 OID 50214)
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 5100 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 225 (class 1259 OID 67427)
-- Name: alugueis; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.alugueis (
                                 id uuid DEFAULT gen_random_uuid() NOT NULL,
                                 sala_id uuid NOT NULL,
                                 inquilino_id uuid NOT NULL,
                                 data_inicio date DEFAULT CURRENT_DATE NOT NULL,
                                 dia_vencimento_padrao smallint NOT NULL,
                                 valor_mensal numeric(12,2) NOT NULL,
                                 status character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
                                 CONSTRAINT aluguel_dia_vencimento_check CHECK (((dia_vencimento_padrao >= 1) AND (dia_vencimento_padrao <= 31))),
                                 CONSTRAINT aluguel_status_check CHECK (((status)::text = ANY ((ARRAY['ATIVO'::character varying, 'ENCERRADO'::character varying])::text[]))),
    CONSTRAINT aluguel_valor_check CHECK ((valor_mensal >= (0)::numeric))
);


ALTER TABLE public.alugueis OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 67457)
-- Name: contrato; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.contrato (
                                 id uuid DEFAULT gen_random_uuid() NOT NULL,
                                 aluguel_id uuid NOT NULL,
                                 data_inicio date NOT NULL,
                                 data_fim date NOT NULL,
                                 aviso_antecedencia_dias integer DEFAULT 30 NOT NULL,
                                 status character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
                                 created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                 CONSTRAINT contrato_aviso_check CHECK ((aviso_antecedencia_dias >= 0)),
                                 CONSTRAINT contrato_datas_check CHECK ((data_fim > data_inicio)),
                                 CONSTRAINT contrato_status_check CHECK (((status)::text = ANY ((ARRAY['ATIVO'::character varying, 'RENOVADO'::character varying, 'ENCERRADO'::character varying])::text[])))
);


ALTER TABLE public.contrato OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 67359)
-- Name: endereco; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.endereco (
                                 id uuid DEFAULT gen_random_uuid() NOT NULL,
                                 zip_code character varying(8) NOT NULL,
                                 estado character varying(2) NOT NULL,
                                 cidade character varying(255) NOT NULL,
                                 bairro character varying(255) NOT NULL,
                                 rua character varying(255) NOT NULL,
                                 numero character varying(10) NOT NULL,
                                 complemento character varying(255),
                                 latitude numeric(10,8),
                                 longitude numeric(11,8),
                                 created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                 updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.endereco OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 67378)
-- Name: galeria; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.galeria (
                                id uuid DEFAULT gen_random_uuid() NOT NULL,
                                nome character varying(255) NOT NULL,
                                phone character varying(20),
                                endereco_id uuid NOT NULL,
                                dono_id uuid NOT NULL
);


ALTER TABLE public.galeria OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 67507)
-- Name: gasto_extra; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.gasto_extra (
                                    id uuid DEFAULT gen_random_uuid() NOT NULL,
                                    galeria_id uuid NOT NULL,
                                    sala_id uuid,
                                    nome character varying(150) NOT NULL,
                                    descricao text,
                                    valor numeric(12,2) NOT NULL,
                                    data_gasto date NOT NULL,
                                    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                    CONSTRAINT gasto_extra_valor_check CHECK ((valor >= (0)::numeric))
);


ALTER TABLE public.gasto_extra OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 67481)
-- Name: pagamento; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pagamento (
                                  id uuid DEFAULT gen_random_uuid() NOT NULL,
                                  aluguel_id uuid NOT NULL,
                                  competencia date NOT NULL,
                                  valor numeric(12,2) NOT NULL,
                                  data_vencimento date NOT NULL,
                                  data_pagamento date,
                                  status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
                                  aviso_7_dias_enviado boolean DEFAULT false NOT NULL,
                                  created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                  CONSTRAINT pagamento_status_check CHECK (((status)::text = ANY ((ARRAY['PENDENTE'::character varying, 'PAGO'::character varying, 'ATRASADO'::character varying])::text[]))),
    CONSTRAINT pagamento_valor_check CHECK ((valor >= (0)::numeric))
);


ALTER TABLE public.pagamento OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 67400)
-- Name: salas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.salas (
                              id uuid DEFAULT gen_random_uuid() NOT NULL,
                              nome character varying(255) NOT NULL,
                              galeria_id uuid NOT NULL
);


ALTER TABLE public.salas OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 67414)
-- Name: tenant; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tenant (
                               id uuid DEFAULT gen_random_uuid() NOT NULL,
                               name character varying(150) NOT NULL,
                               phone character varying(20),
                               email character varying(150),
                               document_number character varying(14),
                               document_type character varying(11),
                               ativo boolean DEFAULT true NOT NULL,
                               CONSTRAINT tenant_document_type_check CHECK (((document_type)::text = ANY ((ARRAY['CPF'::character varying, 'CNPJ'::character varying])::text[])))
);


ALTER TABLE public.tenant OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 67341)
-- Name: usuario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuario (
                                id uuid DEFAULT gen_random_uuid() NOT NULL,
                                nome character varying(255) NOT NULL,
                                email character varying(255) NOT NULL,
                                senha character varying(255) NOT NULL,
                                role character varying(30) DEFAULT 'usuario'::character varying NOT NULL,
                                phone character varying(20) NOT NULL,
                                CONSTRAINT user_role_check CHECK (((role)::text = ANY ((ARRAY['DONO'::character varying, 'ADMINISTRADOR'::character varying])::text[])))
);


ALTER TABLE public.usuario OWNER TO postgres;

--
-- TOC entry 4928 (class 2606 OID 67444)
-- Name: alugueis alugueis_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alugueis
    ADD CONSTRAINT alugueis_pkey PRIMARY KEY (id);


--
-- TOC entry 4932 (class 2606 OID 67475)
-- Name: contrato contrato_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contrato
    ADD CONSTRAINT contrato_pkey PRIMARY KEY (id);


--
-- TOC entry 4916 (class 2606 OID 67377)
-- Name: endereco endereco_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.endereco
    ADD CONSTRAINT endereco_pkey PRIMARY KEY (id);


--
-- TOC entry 4918 (class 2606 OID 67389)
-- Name: galeria galeria_endereco_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.galeria
    ADD CONSTRAINT galeria_endereco_id_key UNIQUE (endereco_id);


--
-- TOC entry 4920 (class 2606 OID 67387)
-- Name: galeria galeria_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.galeria
    ADD CONSTRAINT galeria_pkey PRIMARY KEY (id);


--
-- TOC entry 4938 (class 2606 OID 67522)
-- Name: gasto_extra gasto_extra_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gasto_extra
    ADD CONSTRAINT gasto_extra_pkey PRIMARY KEY (id);


--
-- TOC entry 4934 (class 2606 OID 67501)
-- Name: pagamento pagamento_competencia_unique; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pagamento
    ADD CONSTRAINT pagamento_competencia_unique UNIQUE (aluguel_id, competencia);


--
-- TOC entry 4936 (class 2606 OID 67499)
-- Name: pagamento pagamento_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pagamento
    ADD CONSTRAINT pagamento_pkey PRIMARY KEY (id);


--
-- TOC entry 4922 (class 2606 OID 67408)
-- Name: salas salas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.salas
    ADD CONSTRAINT salas_pkey PRIMARY KEY (id);


--
-- TOC entry 4924 (class 2606 OID 67426)
-- Name: tenant tenant_document_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tenant
    ADD CONSTRAINT tenant_document_number_key UNIQUE (document_number);


--
-- TOC entry 4926 (class 2606 OID 67424)
-- Name: tenant tenant_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tenant
    ADD CONSTRAINT tenant_pkey PRIMARY KEY (id);


--
-- TOC entry 4912 (class 2606 OID 67358)
-- Name: usuario usuario_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_email_key UNIQUE (email);


--
-- TOC entry 4914 (class 2606 OID 67356)
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);


--
-- TOC entry 4929 (class 1259 OID 67455)
-- Name: uq_aluguel_ativo_sala; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uq_aluguel_ativo_sala ON public.alugueis USING btree (sala_id) WHERE ((status)::text = 'ATIVO'::text);


--
-- TOC entry 4930 (class 1259 OID 67456)
-- Name: uq_aluguel_ativo_tenant; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uq_aluguel_ativo_tenant ON public.alugueis USING btree (inquilino_id) WHERE ((status)::text = 'ATIVO'::text);


--
-- TOC entry 4942 (class 2606 OID 67450)
-- Name: alugueis alugueis_inquilino_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alugueis
    ADD CONSTRAINT alugueis_inquilino_id_fkey FOREIGN KEY (inquilino_id) REFERENCES public.tenant(id);


--
-- TOC entry 4943 (class 2606 OID 67445)
-- Name: alugueis alugueis_sala_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alugueis
    ADD CONSTRAINT alugueis_sala_id_fkey FOREIGN KEY (sala_id) REFERENCES public.salas(id);


--
-- TOC entry 4944 (class 2606 OID 67476)
-- Name: contrato contrato_aluguel_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contrato
    ADD CONSTRAINT contrato_aluguel_id_fkey FOREIGN KEY (aluguel_id) REFERENCES public.alugueis(id);


--
-- TOC entry 4939 (class 2606 OID 67395)
-- Name: galeria galeria_dono_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.galeria
    ADD CONSTRAINT galeria_dono_id_fkey FOREIGN KEY (dono_id) REFERENCES public.usuario(id);


--
-- TOC entry 4940 (class 2606 OID 67390)
-- Name: galeria galeria_endereco_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.galeria
    ADD CONSTRAINT galeria_endereco_id_fkey FOREIGN KEY (endereco_id) REFERENCES public.endereco(id);


--
-- TOC entry 4946 (class 2606 OID 67523)
-- Name: gasto_extra gasto_extra_galeria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gasto_extra
    ADD CONSTRAINT gasto_extra_galeria_id_fkey FOREIGN KEY (galeria_id) REFERENCES public.galeria(id);


--
-- TOC entry 4947 (class 2606 OID 67528)
-- Name: gasto_extra gasto_extra_sala_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gasto_extra
    ADD CONSTRAINT gasto_extra_sala_id_fkey FOREIGN KEY (sala_id) REFERENCES public.salas(id);


--
-- TOC entry 4945 (class 2606 OID 67502)
-- Name: pagamento pagamento_aluguel_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pagamento
    ADD CONSTRAINT pagamento_aluguel_id_fkey FOREIGN KEY (aluguel_id) REFERENCES public.alugueis(id);


--
-- TOC entry 4941 (class 2606 OID 67409)
-- Name: salas salas_galeria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.salas
    ADD CONSTRAINT salas_galeria_id_fkey FOREIGN KEY (galeria_id) REFERENCES public.galeria(id);


-- Completed on 2026-09-26 22:04:02

--
-- PostgreSQL database dump complete
--