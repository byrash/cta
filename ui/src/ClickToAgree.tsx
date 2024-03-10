import React, {useEffect, useState} from "react";
import axios from "axios";

function ClickToAgree(props: any) {
    const [render, setRender] = useState<boolean>(false)

    useEffect(() => {
        if (props.filleFormID) {
            setRender(true)
            window.scrollTo({top: 0, behavior: 'smooth'});
        } else {
            setRender(false)
        }
    }, [props.filleFormID])

    function handleSubmit(event: any) {
        event.preventDefault()
        const url = 'http://localhost:8080/api/cta/' + props.filleFormID;
        axios.post(url, {}, {}).then((response) => {
            let cocURL = 'http://localhost:8080/api/coc/' + response.data;
            window.open(cocURL)
        }).catch(reason => console.log(reason));
    }

    // @ts-ignore
    return (
        render ? (<form onSubmit={handleSubmit}>
            <iframe src={props.filledFormURL} width="90%" height={900}/>
            <br/>
            <button type="submit">Agree</button>
        </form>) : <span/>
    );
}

export default ClickToAgree;