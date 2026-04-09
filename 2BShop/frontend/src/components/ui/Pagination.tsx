type PaginationProps = {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
};

export function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) {
    return null;
  }

  const pages = Array.from({ length: totalPages }, (_, index) => index);

  return (
    <div className="pagination">
      <button
        className="button button-subtle"
        disabled={currentPage === 0}
        onClick={() => onPageChange(currentPage - 1)}
        type="button"
      >
        Trước
      </button>
      {pages.map((page) => (
        <button
          key={page}
          className={`button ${page === currentPage ? "button-primary" : "button-subtle"}`}
          onClick={() => onPageChange(page)}
          type="button"
        >
          {page + 1}
        </button>
      ))}
      <button
        className="button button-subtle"
        disabled={currentPage >= totalPages - 1}
        onClick={() => onPageChange(currentPage + 1)}
        type="button"
      >
        Sau
      </button>
    </div>
  );
}
