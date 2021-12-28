-- Table: public.users_documents

-- DROP TABLE public.users_documents;

CREATE TABLE public.users_documents
(
    user_id integer NOT NULL,
    document_id character varying COLLATE pg_catalog."default" NOT NULL,
    permissions character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT users_documents_pkey PRIMARY KEY (user_id, document_id),
    CONSTRAINT user_id_fk FOREIGN KEY (user_id)
        REFERENCES public.app_user (id) MATCH SIMPLE
        ON UPDATE RESTRICT
        ON DELETE CASCADE
        NOT VALID
)

TABLESPACE pg_default;

ALTER TABLE public.users_documents
    OWNER to postgres;