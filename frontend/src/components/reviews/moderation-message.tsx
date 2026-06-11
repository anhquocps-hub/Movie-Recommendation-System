interface ModerationMessageProps {
  hidden?: boolean;
  deleted?: boolean;
  type?: "review" | "reply";
}

export function ModerationMessage({ hidden, deleted, type = "review" }: ModerationMessageProps) {
  if (deleted) {
    return (
      <p className="text-sm italic text-text-dim">
        This {type === "reply" ? "reply has" : "comment has"} been deleted
      </p>
    );
  }

  if (hidden) {
    return (
      <p className="text-sm italic text-text-dim">
        This {type === "reply" ? "reply has" : "comment has"} been hidden
      </p>
    );
  }

  return null;
}
