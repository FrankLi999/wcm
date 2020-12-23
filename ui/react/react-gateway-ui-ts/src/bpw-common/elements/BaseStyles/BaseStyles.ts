import { createStyles, makeStyles } from '@material-ui/core';

const useStyles = makeStyles(() =>
  createStyles({
    '@global': {
      'blockquote, dl, dd, h1, h2, h3, h4, h5, h6, hr, figure, p, pre': {
        margin: 0,
      },
      button: {
        background: 'transparent',
        padding: 0,
      },
      'button:focus': {
        outline: 'none',
      },

      fieldset: {
        margin: 0,
        padding: 0,
      },

      'ol, ul': {
        'list-style': 'none',
        margin: 0,
        padding: 0,
      },
      html: {
        'font-family':
          '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji"',
        'line-height': '1.5',
      },
      '*, ::before, ::after': {
        'box-sizing': 'border-box',
        'border-width': 0,
        'border-style': 'solid',
      },
      hr: {
        'border-top-width': '1px',
      },
      img: {
        'border-style': 'solid',
      },
      textarea: {
        resize: 'vertical',
      },
      'input::placeholder, textarea::placeholder': {
        color: '#A0AEC0',
      },
      'button, [role="button"]': {
        cursor: 'pointer',
      },
      table: {
        'border-collapse': 'collapse',
      },
      'h1, h2, h3, h4, h5, h6': {
        'font-size': 'inherit',
        'font-weight': 'inherit',
      },
      a: {
        color: 'inherit',
        'text-decoration': 'inherit',
      },
      'button, input, optgroup, select, textarea': {
        padding: 0,
        'line-height': 'inherit',
        color: 'inherit',
      },
      'pre, code, kbd, samp': {
        'font-family': 'Consolas, "Liberation Mono", Menlo, Courier, monospace',
      },
      'img, svg, video, canvas, audio, iframe, embed, object': {
        display: 'block',
        'vertical-align': 'middle',
      },
      'img, video': {
        'max-width': '100%',
        height: 'auto',
      },
    },
  })
);

const BaseStyles = () => {
  useStyles();

  return null;
};

export default BaseStyles;
