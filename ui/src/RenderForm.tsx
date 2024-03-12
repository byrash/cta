import React, {useEffect, useState} from 'react';
import './App.css';
import {Form} from '@bpmn-io/form-js';
import "@bpmn-io/form-js/dist/assets/form-js.css";
import axios from "axios";

function RenderForm(props: any) {
    const [renderForm, setRenderForm] = useState<Form>()
    const [init, setInit] = useState<boolean>(true)

    useEffect(() => {
        if (init) {
            setRenderForm(new Form({
                container: document.querySelector('#renderForm')
            }));
            setInit(false)
        }
        renderForm?.clear();
        renderForm?.reset();
        // @ts-ignore
        // renderForm?.on("submit", ({data, errors}) => {
        //     console.log(data, errors);
        // });
        renderForm?.importSchema(props.schema,props.schema.data)
            .catch((error) => console.error("cannot import form schema", error));
    }, [props.schema]);

    function handleSubmit(event: any) {
        event.preventDefault()
        // @ts-ignore
        const {data, errors} = renderForm?.submit();
        if (Object.keys(errors).length) {
            console.error('Form submitted with errors', errors);
        }
        const config = {
            headers: {
                'content-type': 'application/json',
                'Access-Control-Allow-Origin': '*',
            },
        };
        const url = 'http://localhost:8080/api/data/' + props.schema.templateId;
        axios.post(url, data, config).then((response) => {
            // FileDownload(response.data, 'filled_form', "application/pdf");
            let filledFormURL = 'http://localhost:8080/api/cta/' + response.data;
            props.onSubmission(response.data)
            // window.open(filledFormURL)
        }).catch(reason => console.log(reason));


    }

    return (
        <form onSubmit={handleSubmit}>
            <div id="renderForm"></div>
            <button type="submit">Submit</button>
        </form>

    );
}

export default RenderForm;
