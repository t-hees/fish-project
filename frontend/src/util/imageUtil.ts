export function encodeImage(imageData: Blob, callback: (arg0: string | null) => any) {
  const reader = new FileReader();
  reader.onloadend = () => {
    callback(reader.result ? reader.result.toString(): null);
  };
  reader.readAsDataURL(imageData);
}
