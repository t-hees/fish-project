import { useLocation } from "react-router-dom";
import { NotifiableContainer, type NotifiableContentContext } from "../components/NotifiableContainer";

export default function EditFish() {
  return (
    <NotifiableContainer MainContent={Fish} />
  )
}

function Fish({ setError }: NotifiableContentContext) {
  const tripId = new URLSearchParams(useLocation().search).get("id");

  if (!tripId) return (<h1>ERROR: No trip id parameter provided</h1>);

  return (
    <>
      
    </>
  )
}
