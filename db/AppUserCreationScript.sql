CREATE SEQUENCE public.app_user_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.app_user_id_seq
    OWNER TO postgres;


-- Table: public.app_user

-- DROP TABLE public.app_user;

CREATE TABLE public.app_user
(
    name character varying COLLATE pg_catalog."default" NOT NULL,
    id bigint NOT NULL DEFAULT nextval('app_user_id_seq'::regclass),
    pass character varying COLLATE pg_catalog."default" NOT NULL,
    email character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT app_user_pkey PRIMARY KEY (id),
    CONSTRAINT unique_email UNIQUE (email)
        INCLUDE(email),
    CONSTRAINT unique_name UNIQUE (name)
        INCLUDE(name)
)

TABLESPACE pg_default;

ALTER TABLE public.app_user
    OWNER to postgres;